package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.id.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface EventBuilder<E extends EventBuilder<E>> {
	EventBuilder setId(EventId id);

	EventBuilder setThread(InstanceId thread);

	EventBuilder setEvent(int event);

	EventBuilder setClazz(ClazzId clazz);

	EventBuilder setMethod(MethodId method);

	EventBuilder setField(FieldId field);

	EventBuilder addArg(InstanceId arg);
}
