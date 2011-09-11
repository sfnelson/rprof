package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 11/09/11
 */
public class EventManager {

	public interface EventBuilder {
		EventBuilder setId(EventId id);

		EventBuilder setThread(ObjectId thread);

		EventBuilder setEvent(int event);

		EventBuilder setClazz(ClassId clazz);

		EventBuilder setMethod(MethodId method);

		EventBuilder setField(FieldId field);

		EventBuilder addArg(ObjectId arg);

		void store();
	}

	@VisibleForTesting
	@Autowired(required = true)
	Database database;

	public EventBuilder getBuilder() {
		return database.getEventBuilder();
	}
}
