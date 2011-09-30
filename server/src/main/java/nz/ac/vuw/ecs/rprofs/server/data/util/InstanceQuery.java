package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
public interface InstanceQuery<I extends InstanceQuery<I>> extends InstanceBuilder<I>, Query<InstanceId, Instance> {
}
