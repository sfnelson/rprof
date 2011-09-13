package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ClassManager {

	public interface ClassBuilder {
		ClassBuilder setName(String name);

		ClassBuilder setParent(ClazzId parent);

		ClassBuilder setParentName(String name);

		ClassBuilder setProperties(int properties);

		FieldBuilder addField();

		MethodBuilder addMethod();

		ClazzId store();

		Clazz get();
	}

	public interface FieldBuilder {
		FieldBuilder setName(String name);

		FieldBuilder setDescription(String name);

		FieldBuilder setAccess(int access);

		FieldId store();

		Field get();
	}

	public interface MethodBuilder {
		MethodBuilder setName(String name);

		MethodBuilder setDescription(String name);

		MethodBuilder setAccess(int access);

		MethodId store();

		Method get();
	}

	private final org.slf4j.Logger log = LoggerFactory.getLogger(ClassManager.class);

	@VisibleForTesting
	@Autowired(required = true)
	Context context;

	@VisibleForTesting
	@Autowired(required = true)
	Database database;

	private final Map<String, List<ClazzId>> awaitingSuper = Collections.newMap();

	public ClassBuilder createClass() {
		return database.getClassBuilder();
	}

	public Clazz findClass(ClazzId id) {
		return database.findEntity(id);
	}

	public List<Field> findFields(ClazzId id) {
		return database.findEntities(Field.class, id);
	}

	public List<Method> findMethods(ClazzId id) {
		return database.findEntities(Method.class, id);
	}

	public Clazz findClass(String name) {
		List<Clazz> classes = database.findEntities(Clazz.class, name);
		if (classes == null || classes.isEmpty()) return null;
		else return classes.get(0);
	}

	public void setProperties(ClazzId clazzId, int properties) {
		database.getClassUpdater(clazzId).setProperties(properties).store();
	}

	public Clazz storeClass(Clazz cls) {

		log.debug("storing new class {} ({})", cls.getName(), cls.getId());


		if (cls.getParentName() != null) {
			Clazz parent = findClass(cls.getParentName());

			if (parent != null) {
				cls.setParent(parent.getId());
			} else {
				List<ClazzId> list = awaitingSuper.get(cls.getParentName());
				if (list == null) {
					list = Collections.newList();
					awaitingSuper.put(cls.getParentName(), list);
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