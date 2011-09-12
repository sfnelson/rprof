package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import nz.ac.vuw.ecs.rprofs.client.request.id.EventIdProxy;
import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;

import java.util.List;

@Service(value = EventService.class, locator = ServiceLocator.class)
public interface EventRequest extends RequestContext {

	Request<List<EventProxy>> findEvents(Integer start, Integer length, Integer filter);

	Request<Long> findIndexOf(EventIdProxy event, Integer filter);

	Request<List<EventProxy>> findEventsByInstance(Long instanceId);

	Request<Long> findNumEvents(Integer filter);

	Request<Long> findNumThreads();

	Request<List<InstanceProxy>> findThreads();
}
