package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import nz.ac.vuw.ecs.rprofs.client.request.id.ClazzIdProxy;
import nz.ac.vuw.ecs.rprofs.server.request.MethodService;
import nz.ac.vuw.ecs.rprofs.server.request.ServiceLocator;

import java.util.List;


@Service(value = MethodService.class, locator = ServiceLocator.class)
public interface MethodRequest extends RequestContext {

	Request<List<? extends MethodProxy>> findMethods(ClazzIdProxy cls);

}
