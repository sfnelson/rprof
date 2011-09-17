package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import nz.ac.vuw.ecs.rprofs.client.request.id.ClazzIdProxy;
import nz.ac.vuw.ecs.rprofs.server.request.ClazzService;
import nz.ac.vuw.ecs.rprofs.server.request.ServiceLocator;

import java.util.List;

@Service(value = ClazzService.class, locator = ServiceLocator.class)
public interface ClazzRequest extends RequestContext {

	Request<Long> findNumPackages();

	Request<List<String>> findPackages();

	Request<Long> findNumClasses();

	Request<Long> findNumClasses(String packageName);

	Request<List<ClazzProxy>> findClasses();

	Request<List<ClazzProxy>> findClasses(String packageName);

	Request<ClazzProxy> getClazz(ClazzIdProxy id);

	Request<ClazzProxy> getClazz(String className);

}
