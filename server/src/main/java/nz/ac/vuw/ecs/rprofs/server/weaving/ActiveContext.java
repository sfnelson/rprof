package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.request.ClassService;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Configurable
public class ActiveContext {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(ActiveContext.class);

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private ClassService classes;

	@Autowired
	private DatasetService datasets;

	private DataSet dataSet;

	private long eventId = 0;
	private int classId = 0;

	private final Map<String, List<ClassId>> awaitingSuper = Collections.newMap();

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	public void setMainMethod(String program) {
		dataSet = datasets.setProgram(dataSet, program);
	}

	public EventId nextEvent() {
		return new EventId(dataSet.getId().indexValue(), ++eventId);
	}

	public ClassId nextClass() {
		return new ClassId(dataSet.getId().indexValue(), ++classId);
	}

	@Transactional
	public Clazz storeClass(ClassRecord cr) {
		Clazz cls = cr.toClass(dataSet);
		em.persist(cls);

		log.debug("storing new class {} ({})", cls.getName(), cls.getId());

		for (FieldRecord fr: cr.getFields().values()) {
			em.persist(fr.toAttribute(cls));
		}

		for (MethodRecord mr: cr.getMethods().values()) {
			em.persist(mr.toAttribute(cls));
		}

		if (cr.getSuperName() != null) {
			Clazz parent = classes.findClass(cr.getSuperName());

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
				Clazz c = em.find(Clazz.class, cid);
				if (c != null) {
					c.setParent(cls);
				}
				else {
					log.warn("could not find class id {} with parent {} ({}) [{} {}]", new Object[] {
							cid.toString(), cls.getName(), cls.getId().toString(), cid.longValue(), cls.getId().longValue()});
				}
			}
		}

		em.persist(new Event(dataSet, nextEvent(), null, Event.CLASS_WEAVE, cls, null, new ArrayList<Instance>()));

		log.debug("finished storing new class {} ({})", cls.getName(), cls.getId());

		return cls;
	}
}