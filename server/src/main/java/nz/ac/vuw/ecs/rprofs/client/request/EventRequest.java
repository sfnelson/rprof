package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value = EventService.class, locator = ServiceLocator.class)
public interface EventRequest extends RequestContext {

	Request<List<EventProxy>> findEvents(int start, int length, int filter);
	Request<Long> findIndexOf(long event, int filter);
	Request<List<EventProxy>> findEventsByInstance(long instanceId);
	Request<Long> findNumEvents(int filter);

}
