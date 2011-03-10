package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.EventService;
import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value = EventService.class, locator = ServiceLocator.class)
public interface EventRequest extends RequestContext {

	Request<List<EventProxy>> findEvents(String dataset, int start, int length);

	Request<List<EventProxy>> findEventsByInstance(InstanceProxy instanceProxy);

}
