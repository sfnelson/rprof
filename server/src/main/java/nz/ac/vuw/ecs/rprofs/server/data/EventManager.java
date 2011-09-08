package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;

public class EventManager implements EventService {

	@Override
	public List<? extends Event> findEvents(Integer start, Integer length, Integer filter) {
		return null;
	}

	@Override
	public List<? extends Event> findEventsByInstance(Long instance) {
		return null;
	}

	@Override
	public Long findNumEvents(Integer filter) {
		return null;
	}

	@Override
	public Long findIndexOf(Long eventId, Integer filter) {
		return null;
	}

	@Override
	public Long findNumThreads() {
		return null;
	}

	@Override
	public List<Instance> findThreads() {
		return null;
	}

	private DataSet owner() {
		return ContextManager.getThreadLocal();
	}
}
