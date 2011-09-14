package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import nz.ac.vuw.ecs.rprofs.server.request.InstanceService;
import nz.ac.vuw.ecs.rprofs.server.request.ServiceLocator;

import java.util.List;

@Service(value = InstanceService.class, locator = ServiceLocator.class)
public interface InstanceRequest extends RequestContext {

	Request<InstanceProxy> findInstance(Long instanceId);

	Request<List<? extends InstanceProxy>> findInstancesForClass(ClazzProxy cls);

}
