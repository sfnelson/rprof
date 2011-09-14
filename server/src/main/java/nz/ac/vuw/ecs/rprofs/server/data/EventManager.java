package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 11/09/11
 */
public class EventManager {

	public interface EventBuilder<E extends EventBuilder<E>> {
		EventBuilder setId(EventId id);

		EventBuilder setThread(InstanceId thread);

		EventBuilder setEvent(int event);

		EventBuilder setClazz(ClazzId clazz);

		EventBuilder setMethod(MethodId method);

		EventBuilder setField(FieldId field);

		EventBuilder addArg(InstanceId arg);
	}

	public interface EventCreator<E extends EventCreator<E>> extends EventBuilder<E>, Creator<EventId, Event> {
	}

	public interface EventUpdater<E extends EventUpdater<E>> extends EventBuilder<E>, Updater<EventId, Event> {
	}

	public interface EventQuery<E extends EventQuery<E>> extends EventBuilder<E>, Query<EventId, Event> {
	}

	@VisibleForTesting
	@Autowired(required = true)
	Database database;

	public EventCreator createEvent() {
		return database.getEventCreater();
	}
}
