package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
public interface InstanceBuilder<I extends InstanceBuilder<I>> extends Builder<I, InstanceId, Instance> {

	I setId(InstanceId id);

	I setType(ClazzId type);

	I setConstructor(MethodId constructor);

	I setConstructorReturn(EventId constructorReturn);

	I setFirstEquals(EventId equals);

	I setFirstHashCode(EventId hashCode);

	I addFieldInfo(Instance.FieldInfo info);
}
