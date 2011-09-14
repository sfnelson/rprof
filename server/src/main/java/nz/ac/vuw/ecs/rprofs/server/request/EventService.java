package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;

import java.util.List;

public interface EventService {

	List<Event> findEvents(Integer start, Integer length, Integer filter);

	Long findIndexOf(EventId eventId, Integer filter);

	List<Event> findEventsByInstance(Long instanceId);

	Long findNumEvents(Integer filter);

	Long findNumThreads();

	List<Instance> findThreads();
}
