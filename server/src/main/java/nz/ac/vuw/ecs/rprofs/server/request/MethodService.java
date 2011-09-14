package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;

import java.util.List;

public interface MethodService {

	List<? extends Method> findMethods(ClazzId clazzId);

}
