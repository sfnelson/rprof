package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.request.EventRequest;

public class EventService extends AbstractService implements EventRequest {

	@Override
	public List<? extends Event> findEvents(String dataset, int start, int length) {
		return context(dataset).findEvents(start, length);
	}

	@Override
	public List<? extends Event> findEventsByInstance(Instance instance) {
		return instance.getEvents();
	}

}
