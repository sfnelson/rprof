package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import nz.ac.vuw.ecs.rprofs.client.request.id.ClazzIdProxy;
import nz.ac.vuw.ecs.rprofs.server.request.FieldService;
import nz.ac.vuw.ecs.rprofs.server.request.ServiceLocator;

import java.util.List;

@Service(value = FieldService.class, locator = ServiceLocator.class)
public interface FieldRequest extends RequestContext {

	Request<List<? extends FieldProxy>> findFields(ClazzIdProxy cls);

}
