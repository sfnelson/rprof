package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import nz.ac.vuw.ecs.rprofs.client.request.id.EventIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.InstanceIdProxy;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;
import nz.ac.vuw.ecs.rprofs.server.request.ServiceLocator;

import java.util.List;

@Service(value = EventService.class, locator = ServiceLocator.class)
public interface EventRequest extends RequestContext {

	Request<List<EventProxy>> findEvents(int start, int length, int filter);

	Request<Long> findIndexOf(EventIdProxy event, int filter);

	Request<List<EventProxy>> findEventsByInstance(InstanceIdProxy instanceId);

	Request<Long> findNumEvents(int filter);

	Request<Long> findNumThreads();

	Request<List<InstanceIdProxy>> findThreads();
}
