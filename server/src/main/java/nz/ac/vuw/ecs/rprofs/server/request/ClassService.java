package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;

public interface ClassService {

	int findNumPackages();
	List<String> findPackages();

	int findNumClasses(String pkg);
	List<? extends Class> findClasses(String pkg);

	int findNumClasses();
	List<? extends Class> findClasses();

}
