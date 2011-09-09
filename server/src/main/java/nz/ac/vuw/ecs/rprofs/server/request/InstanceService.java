package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

import java.util.List;

public interface InstanceService {

	Instance findInstance(Long id);

	int findNumInstancesForClass(Clazz cls);

	List<? extends Instance> findInstancesForClass(Clazz cls);

	int findNumInstances();

	List<? extends Instance> findInstances();

}
