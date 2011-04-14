package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;

public interface EventRequest {

	Long findIndexOf(long eventId, int filter);

	Long findNumEvents(String dataset, int filter);
	List<? extends Event> findEvents(String dataset, int start, int length, int filter);
	List<? extends Event> findEventsByInstance(long instance);

}
