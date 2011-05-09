package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

public interface EventService {

	List<? extends Event> findEvents(Integer start, Integer length, Integer filter);
	Long findIndexOf(Long eventId, Integer filter);
	List<? extends Event> findEventsByInstance(Long instance);
	Long findNumEvents(Integer filter);

	Long findNumThreads();
	List<Instance> findThreads();
}
