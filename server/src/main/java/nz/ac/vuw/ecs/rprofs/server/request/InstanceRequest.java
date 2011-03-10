package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

public interface InstanceRequest {

	Instance findInstance(Long id);
	List<? extends Instance> findInstances(Class cls);

}
