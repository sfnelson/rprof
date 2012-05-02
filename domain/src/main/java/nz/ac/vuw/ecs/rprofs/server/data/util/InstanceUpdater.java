package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
public interface InstanceUpdater<I extends InstanceUpdater<I>>
		extends InstanceBuilder<I>, Updater<I, InstanceId, Instance> {
}
