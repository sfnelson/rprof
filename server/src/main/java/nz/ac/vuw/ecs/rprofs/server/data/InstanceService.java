package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.request.InstanceRequest;

public class InstanceService extends AbstractService implements InstanceRequest {

	@Override
	public Instance findInstance(Long id) {
		ObjectId oid = new ObjectId(id);
		return context(oid.getDataset()).db.findRecord(Instance.class, oid);
	}

	@Override
	public List<? extends Instance> findInstances(Class cls) {
		return context(cls.getId().getDataset()).findInstances(cls.getId());
	}

}
