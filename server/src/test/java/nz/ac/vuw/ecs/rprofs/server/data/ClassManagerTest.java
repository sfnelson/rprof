package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Test;

import java.util.Date;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/09/11
 */
public class ClassManagerTest {

	ClassManager cm;
	Context context;
	Dataset dataset;
	Database database;
	ClassManager.ClassBuilder builder;

	@org.junit.Before
	public void setup() {
		context = createMock(Context.class);
		database = createMock(Database.class);
		builder = createMock(ClassManager.ClassBuilder.class);

		dataset = new Dataset(new DatasetId((short) 1), "foo", new Date());

		cm = new ClassManager();
		cm.context = context;
		cm.database = database;
	}

	@Test
	public void testCreateClass() throws Exception {

		expect(database.getClassBuilder()).andReturn(builder);

		replay(context, database, builder);

		ClassManager.ClassBuilder result = cm.createClass();

		verify(context, database, builder);

		assertEquals(builder, result);
	}

	@Test
	public void testUpdateClass() throws Exception {

	}

	@Test
	public void testStoreClass() throws Exception {

	}
}
