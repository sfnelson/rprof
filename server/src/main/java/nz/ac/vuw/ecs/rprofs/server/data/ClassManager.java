package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.weaving.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.weaving.FieldRecord;
import nz.ac.vuw.ecs.rprofs.server.weaving.MethodRecord;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ClassManager {

	public interface ClassBuilder {
		ClassBuilder setName(String name);

		ClassBuilder setParent(ClazzId parent);

		ClassBuilder setProperties(int properties);

		ClazzId store();
	}

	private final org.slf4j.Logger log = LoggerFactory.getLogger(ClassManager.class);

	@VisibleForTesting
	@Autowired(required = true)
	Context context;

	@VisibleForTesting
	@Autowired(required = true)
	Database database;

	private final Map<String, List<ClazzId>> awaitingSuper = Collections.newMap();

	public ClazzId createClass() {
		return database.getClassBuilder().store();
	}

	public Clazz findClass(String name) {
		List<Clazz> classes = database.findEntities(Clazz.class, name);
		if (classes == null || classes.isEmpty()) return null;
		else return classes.get(0);
	}

	public Clazz storeClass(ClassRecord cr) {
		Clazz cls = cr.toClass();
		// TODO persist class

		log.debug("storing new class {} ({})", cls.getName(), cls.getId());

		for (FieldRecord fr : cr.getFields().values()) {
			// TODO em.persist(fr.toAttribute(cls));
		}

		for (MethodRecord mr : cr.getMethods().values()) {
			// TODO em.persist(mr.toAttribute(cls));
		}

		if (cr.getSuperName() != null) {
			Clazz parent = findClass(cr.getSuperName());

			if (parent != null) {
				cls.setParent(parent.getId());
			} else {
				List<ClazzId> list = awaitingSuper.get(cr.getSuperName());
				if (list == null) {
					list = Collections.newList();
					awaitingSuper.put(cr.getSuperName(), list);
				}
				list.add(cls.getId());
			}
		}

		if (awaitingSuper.containsKey(cls.getName())) {
			for (ClazzId cid : awaitingSuper.remove(cls.getName())) {
				Clazz c = database.findEntity(cid);
				if (c != null) {
					c.setParent(cls.getId());
				} else {
					log.warn("could not find class id {} with parent {} ({}) [{} {}]",
							new Object[]{
									cid, cls.getName(), cls.getId(), cid, cls.getId()
							});
				}
			}
		}

		log.debug("finished storing new class {} ({})", cls.getName(), cls.getId());

		return cls;
	}
}