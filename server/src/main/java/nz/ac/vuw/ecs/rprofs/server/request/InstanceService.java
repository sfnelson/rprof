package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

public interface InstanceService {

	Instance findInstance(Long id);

	int findNumInstances(Class cls);
	List<? extends Instance> findInstances(Class cls);

	int findNumInstances();
	List<? extends Instance> findInstances();

}
