package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

import java.util.List;

public interface MethodService {

	List<? extends Method> findMethods(Clazz cls);

}
