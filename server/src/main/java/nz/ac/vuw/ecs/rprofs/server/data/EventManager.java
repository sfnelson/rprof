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

	public interface EventBuilder {
		EventBuilder setId(EventId id);

		EventBuilder setThread(InstanceId thread);

		EventBuilder setEvent(int event);

		EventBuilder setClazz(ClazzId clazz);

		EventBuilder setMethod(MethodId method);

		EventBuilder setField(FieldId field);

		EventBuilder addArg(InstanceId arg);

		void store();

		Event get();
	}

	@VisibleForTesting
	@Autowired(required = true)
	Database database;

	public EventBuilder getBuilder() {
		return database.getEventBuilder();
	}
}
