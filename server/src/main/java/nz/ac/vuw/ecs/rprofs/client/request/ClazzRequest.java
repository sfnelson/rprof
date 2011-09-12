package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.ClazzService;

import java.util.List;

@Service(value = ClazzService.class, locator = ServiceLocator.class)
public interface ClazzRequest extends RequestContext {

	Request<Integer> findNumPackages();

	Request<List<String>> findPackages();

	Request<Integer> findNumClasses();

	Request<List<ClazzProxy>> findClasses();

	Request<Integer> findNumClassesInPackage(String pkg);

	Request<List<ClazzProxy>> findClassesInPackage(String pkg);

}
