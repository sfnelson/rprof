package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.MethodService;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;


@Service(value = MethodService.class, locator = ServiceLocator.class)
public interface MethodRequest extends RequestContext {

	Request<List<MethodProxy>> findMethods(ClassProxy cls);

}
