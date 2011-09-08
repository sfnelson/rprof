package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;

public interface ClassService {

	Integer findNumPackages();
	List<String> findPackages();

	Integer findNumClassesInPackage(String pkg);
	List<? extends Clazz> findClassesInPackage(String pkg);

	Integer findNumClasses();
	List<? extends Clazz> findClasses();
	Clazz findClass(String fqn);

}
