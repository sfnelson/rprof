package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;

public class EventManager implements EventService {

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<? extends Event> findEvents(Integer start, Integer length, Integer filter) {
		TypedQuery<Event> q = em.createNamedQuery("getEvents", Event.class);
		q.setParameter("owner", owner());
		q.setFirstResult(start);
		q.setMaxResults(length);
		q.setParameter("filter", filter.intValue());
		return q.getResultList();
	}

	@Override
	public List<? extends Event> findEventsByInstance(Long instance) {
		Instance i = em.find(Instance.class, new ObjectId(instance));

		TypedQuery<Event> q = em.createNamedQuery("eventsWithArg", Event.class);
		q.setParameter("owner", owner());
		q.setParameter("instance", i);
		return q.getResultList();
	}

	@Override
	public Long findNumEvents(Integer filter) {
		TypedQuery<Number> q = em.createNamedQuery("numEvents", Number.class);
		q.setParameter("owner", owner());
		q.setParameter("filter", filter.intValue());
		return q.getSingleResult().longValue();
	}

	@Override
	public Long findIndexOf(Long eventId, Integer filter) {
		TypedQuery<Number> q = em.createNamedQuery("numEventsBefore", Number.class);
		q.setParameter("owner", owner());
		q.setParameter("filter", filter.intValue());
		q.setParameter("id", eventId.longValue());
		return q.getSingleResult().longValue();
	}

	@Override
	public Long findNumThreads() {
		TypedQuery<Number> q = em.createNamedQuery("numThreads", Number.class);
		q.setParameter("owner", owner());
		return q.getSingleResult().longValue();
	}

	@Override
	public List<Instance> findThreads() {
		TypedQuery<Instance> q = em.createNamedQuery("getThreads", Instance.class);
		q.setParameter("owner", owner());
		return q.getResultList();
	}

	private Dataset owner() {
		return ContextManager.getThreadLocal();
	}
}
