package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;

public interface EventService {

	List<? extends Event> findEvents(int start, int length, int filter);
	Long findIndexOf(long eventId, int filter);
	List<? extends Event> findEventsByInstance(long instance);
	Long findNumEvents(int filter);

}
