package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;

import java.util.List;

public interface ClazzService {

	long findNumPackages();

	List<String> findPackages();

	long findNumClasses();

	long findNumClasses(String pkg);

	List<? extends Clazz> findClasses();

	List<? extends Clazz> findClasses(String pkg);

	Clazz getClazz(ClazzId id);

	Clazz getClazz(String className);

}
