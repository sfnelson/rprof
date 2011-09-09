package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.request.ClassService;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Configurable
public class ActiveContext {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(ActiveContext.class);

	@Autowired
	private ClassService classes;

	@Autowired
	private DatasetService datasets;

	private Dataset dataset;

	private long eventId = 0;
	private int classId = 0;

	private final Map<String, List<ClassId>> awaitingSuper = Collections.newMap();

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void setMainMethod(String program) {
		dataset = datasets.setProgram(dataset, program);
	}

	public EventId nextEvent() {
		return new EventId(dataset.getId().indexValue(), ++eventId);
	}

	public ClassId nextClass() {
		return new ClassId(dataset.getId().indexValue(), ++classId);
	}

	@Transactional
	public Clazz storeClass(ClassRecord cr) {
		Clazz cls = cr.toClass(dataset);
		// TODO persist class

		log.debug("storing new class {} ({})", cls.getName(), cls.getId());

		for (FieldRecord fr : cr.getFields().values()) {
			// TODO em.persist(fr.toAttribute(cls));
		}

		for (MethodRecord mr : cr.getMethods().values()) {
			// TODO em.persist(mr.toAttribute(cls));
		}

		if (cr.getSuperName() != null) {
			Clazz parent = classes.findClass(cr.getSuperName());

			if (parent != null) {
				cls.setParent(parent);
			} else {
				List<ClassId> list = awaitingSuper.get(cr.getSuperName());
				if (list == null) {
					list = Collections.newList();
					awaitingSuper.put(cr.getSuperName(), list);
				}
				list.add(cls.getId());
			}
		}

		if (awaitingSuper.containsKey(cls.getName())) {
			for (ClassId cid : awaitingSuper.remove(cls.getName())) {
				Clazz c = null; // TODO em.find(Clazz.class, cid);
				if (c != null) {
					c.setParent(cls);
				} else {
					log.warn("could not find class id {} with parent {} ({}) [{} {}]", new Object[]{
							cid.toString(), cls.getName(), cls.getId().toString(), cid.longValue(), cls.getId().longValue()});
				}
			}
		}

		log.debug("finished storing new class {} ({})", cls.getName(), cls.getId());

		return cls;
	}
}