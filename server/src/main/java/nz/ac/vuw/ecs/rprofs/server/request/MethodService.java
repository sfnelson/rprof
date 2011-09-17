package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;

import java.util.List;

public interface MethodService {

	Method getMethod(MethodId methodId);

	List<? extends Method> findMethods(ClazzId clazzId);

}
