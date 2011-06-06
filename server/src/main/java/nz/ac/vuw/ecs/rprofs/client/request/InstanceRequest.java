package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.InstanceService;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value=InstanceService.class, locator=ServiceLocator.class)
public interface InstanceRequest extends RequestContext {

	Request<InstanceProxy> findInstance(Long instanceId);

	Request<List<InstanceProxy>> findInstancesForClass(ClassProxy cls);

}