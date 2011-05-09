package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value = EventService.class, locator = ServiceLocator.class)
public interface EventRequest extends RequestContext {

	Request<List<EventProxy>> findEvents(Integer start, Integer length, Integer filter);
	Request<Long> findIndexOf(Long event, Integer filter);
	Request<List<EventProxy>> findEventsByInstance(Long instanceId);
	Request<Long> findNumEvents(Integer filter);
	Request<Long> findNumThreads();
	Request<List<InstanceProxy>> findThreads();
}
