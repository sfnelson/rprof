package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.data.util.EventCreator;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 11/09/11
 */
public class EventManager {

	@VisibleForTesting
	@Autowired(required = true)
	Database database;

	public EventCreator createEvent() {
		return database.getEventCreater();
	}
}
