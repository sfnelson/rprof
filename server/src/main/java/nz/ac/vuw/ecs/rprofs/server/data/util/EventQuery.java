package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface EventQuery<E extends EventQuery<E>> extends EventBuilder<E>, Query<EventId, Event> {
	E setFilter(int filter);

	E setBefore(EventId eventId);

	E setWithArg(InstanceId instanceId);
}
