package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.request.ClassService;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Configurable
public class ActiveContext {

	private final Logger log = Logger.getLogger("weaver");

	@PersistenceContext
	private EntityManager em;

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
		return new EventId(dataset.getId(), ++eventId);
	}

	public ClassId nextClass() {
		return new ClassId(dataset.getId(), ++classId);
	}

	@Transactional
	public Class storeClass(ClassRecord cr) {
		Class cls = cr.toClass(dataset);
		em.persist(cls);

		log.finest(String.format("storing new class %s (%s)", cls.getName(), cls.getId().toString()));

		for (FieldRecord fr: cr.getFields().values()) {
			em.persist(fr.toAttribute(cls));
		}

		for (MethodRecord mr: cr.getMethods().values()) {
			em.persist(mr.toAttribute(cls));
		}

		if (cr.getSuperName() != null) {
			Class parent = classes.findClass(cr.getSuperName());

			if (parent != null) {
				cls.setParent(parent);
			}
			else {
				List<ClassId> list = awaitingSuper.get(cr.getSuperName());
				if (list == null) {
					list = Collections.newList();
					awaitingSuper.put(cr.getSuperName(), list);
				}
				list.add(cls.getId());
			}
		}

		if (awaitingSuper.containsKey(cls.getName())) {
			for (ClassId cid: awaitingSuper.remove(cls.getName())) {
				Class c = em.find(Class.class, cid);
				if (c != null) {
					c.setParent(cls);
				}
				else {
					log.warning(String.format("could not find class id %s with parent %s (%s) [%d %d]",
							cid.toString(), cls.getName(), cls.getId().toString(), cid.getId(), cls.getId().getId()));
				}
			}
		}

		em.persist(new Event(dataset, nextEvent(), null, Event.CLASS_WEAVE, cls, null, new ArrayList<Instance>()));

		log.finest(String.format("finished storing new class %s (%s)", cls.getName(), cls.getId().toString()));

		return cls;
	}
}