package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.FieldService;
import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value = FieldService.class, locator = ServiceLocator.class)
public interface FieldRequest extends RequestContext {

	Request<List<FieldProxy>> findFields(ClassProxy cls);

}
