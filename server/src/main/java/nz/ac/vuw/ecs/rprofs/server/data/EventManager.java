package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;

public class EventManager extends DomainManager<Event> implements EventService {

	private InstanceManager instances;

	public EventManager() {
		super(Event.class, EventId.class);
	}

	@Override
	public List<? extends Event> findEvents(int start, int length, int filter) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Event> q = em.createNamedQuery("findEvents", Event.class);
		q.setFirstResult(start);
		q.setMaxResults(length);
		q.setParameter("filter", filter);
		return q.getResultList();
	}

	@Override
	public List<? extends Event> findEventsByInstance(long instance) {
		EntityManager em = cm.getCurrent().em();

		Instance i = instances.find(new ObjectId(instance));

		TypedQuery<Event> q = em.createNamedQuery("eventsWithArg", Event.class);
		q.setParameter("instance", i);
		return q.getResultList();
	}

	@Override
	public Long findNumEvents(int filter) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Number> q = em.createNamedQuery("numEvents", Number.class);
		q.setParameter("filter", filter);
		return q.getSingleResult().longValue();
	}

	@Override
	public Long findIndexOf(long eventId, int filter) {
		EntityManager em = cm.getCurrent().em();

		EventId id = new EventId(eventId);

		TypedQuery<Number> q = em.createNamedQuery("numEventsBefore", Number.class);
		q.setParameter("filter", filter);
		q.setParameter("id", id);
		return q.getSingleResult().longValue();
	}

}
