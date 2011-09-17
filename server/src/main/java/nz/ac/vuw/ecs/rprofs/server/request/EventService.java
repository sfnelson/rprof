package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;

import java.util.List;

public interface EventService {

	List<? extends Event> findEvents(int start, int length, int filter);

	long findIndexOf(EventId eventId, int filter);

	List<? extends Event> findEventsByInstance(InstanceId instance);

	long findNumEvents(int filter);

	long findNumThreads();

	List<? extends InstanceId> findThreads();
}
