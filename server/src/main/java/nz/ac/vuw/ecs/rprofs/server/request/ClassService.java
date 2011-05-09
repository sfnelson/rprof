package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;

public interface ClassService {

	Integer findNumPackages();
	List<String> findPackages();

	Integer findNumClassesInPackage(String pkg);
	List<? extends Class> findClassesInPackage(String pkg);

	Integer findNumClasses();
	List<? extends Class> findClasses();

}
