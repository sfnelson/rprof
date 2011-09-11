package nz.ac.vuw.ecs.rprofs.server.data;

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
		manager = new EventManager();
		manager.database = database;
	}

	@Test
	public void testGetBuilder() throws Exception {
		EventManager.EventBuilder builder = createMock(EventManager.EventBuilder.class);

		expect(database.getEventBuilder()).andReturn(builder);

		replay(database, builder);

		manager.getBuilder();

		verify(database, builder);
	}
}