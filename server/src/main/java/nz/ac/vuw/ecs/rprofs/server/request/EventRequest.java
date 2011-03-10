package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

public interface EventRequest {

	List<? extends Event> findEvents(String dataset, int start, int length);

	List<? extends Event> findEventsByInstance(Instance instance);

}
