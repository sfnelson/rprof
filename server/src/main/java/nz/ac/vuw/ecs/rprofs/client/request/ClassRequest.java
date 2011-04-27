package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.ClassService;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value=ClassService.class, locator=ServiceLocator.class)
public interface ClassRequest extends RequestContext {

	Request<List<String>> findPackages();
	Request<List<ClassProxy>> findClasses(String pkg);

}
