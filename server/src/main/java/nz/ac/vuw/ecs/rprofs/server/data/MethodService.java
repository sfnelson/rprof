package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.request.MethodRequest;

public class MethodService extends AbstractService implements MethodRequest {

	@Override
	public List<? extends Method> findMethods(Class cls) {
		return cls.getMethods();
	}

}
