package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;

import java.util.List;

public interface ClazzService {

	Integer findNumPackages();

	List<String> findPackages();

	Integer findNumClassesInPackage(String pkg);

	List<? extends Clazz> findClassesInPackage(String pkg);

	Integer findNumClasses();

	List<? extends Clazz> findClasses();

	Clazz findClass(ClazzId id);

}
