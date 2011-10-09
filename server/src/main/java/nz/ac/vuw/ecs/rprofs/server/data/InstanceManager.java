package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import com.google.web.bindery.requestfactory.shared.Locator;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.validation.constraints.NotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
@Configurable
public class InstanceManager extends Locator<Instance, InstanceId> {

	@VisibleForTesting
	@NotNull
	@Autowired
	Database database;

	@Override
	public Instance create(Class<? extends Instance> aClass) {
		return new Instance();
	}

	@Override
	public Instance find(Class<? extends Instance> aClass, InstanceId instanceId) {
		return database.findEntity(instanceId);
	}

	@Override
	public Class<Instance> getDomainType() {
		return Instance.class;
	}

	@Override
	public InstanceId getId(Instance instance) {
		return instance.getId();
	}

	@Override
	public Class<InstanceId> getIdType() {
		return InstanceId.class;
	}

	@Override
	public Object getVersion(Instance instance) {
		return instance.getVersion();
	}
}
