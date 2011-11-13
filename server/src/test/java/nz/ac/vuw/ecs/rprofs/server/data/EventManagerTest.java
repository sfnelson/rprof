package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.data.util.EventCreator;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public class EventManagerTest {

	Database database;
	EventManager manager;

	@org.junit.Before
	public void setup() {
		database = createMock(Database.class);
		manager = new EventManager(database);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetBuilder() throws Exception {
		EventCreator builder = createMock(EventCreator.class);

		expect(database.getEventCreater()).andReturn(builder);

		replay(database, builder);

		manager.createEvent();

		verify(database, builder);
	}
}
