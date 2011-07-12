package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;

public class EventManager extends DomainManager<Event> implements EventService {

	private final InstanceManager instances;

	public EventManager() {
		this(ContextManager.getInstance());
	}

	public EventManager(ContextManager cm) {
		super(cm, Event.class, EventId.class);
		instances = new InstanceManager(cm);
	}

	@Override
	public List<? extends Event> findEvents(Integer start, Integer length, Integer filter) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Event> q = em.createNamedQuery("getEvents", Event.class);
		q.setFirstResult(start);
		q.setMaxResults(length);
		q.setParameter("filter", filter.intValue());
		return q.getResultList();
	}

	@Override
	public List<? extends Event> findEventsByInstance(Long instance) {
		EntityManager em = cm.getCurrent().em();

		Instance i = instances.find(new ObjectId(instance));

		TypedQuery<Event> q = em.createNamedQuery("eventsWithArg", Event.class);
		q.setParameter("instance", i);
		return q.getResultList();
	}

	@Override
	public Long findNumEvents(Integer filter) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Number> q = em.createNamedQuery("numEvents", Number.class);
		q.setParameter("filter", filter.intValue());
		return q.getSingleResult().longValue();
	}

	@Override
	public Long findIndexOf(Long eventId, Integer filter) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Number> q = em.createNamedQuery("numEventsBefore", Number.class);
		q.setParameter("filter", filter.intValue());
		q.setParameter("id", eventId.longValue());
		return q.getSingleResult().longValue();
	}

	@Override
	public Long findNumThreads() {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Number> q = em.createNamedQuery("numThreads", Number.class);
		return q.getSingleResult().longValue();
	}

	@Override
	public List<Instance> findThreads() {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<ObjectId> q = em.createNamedQuery("getThreads", ObjectId.class);
		List<ObjectId> ids = q.getResultList();

		List<Instance> instances = Collections.newList();
		for (ObjectId id: ids) {
			instances.add(id != null ? this.instances.find(id) : null);
		}
		return instances;
	}

}
