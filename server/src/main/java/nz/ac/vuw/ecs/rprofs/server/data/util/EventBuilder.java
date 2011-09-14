package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.id.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface EventBuilder<E extends EventBuilder<E>> {
	E setId(@NotNull EventId id);

	E setThread(@Nullable InstanceId thread);

	E setEvent(int event);

	E setClazz(@Nullable ClazzId clazz);

	E setMethod(@Nullable MethodId method);

	E setField(@Nullable FieldId field);

	E addArg(@Nullable InstanceId arg);
}
