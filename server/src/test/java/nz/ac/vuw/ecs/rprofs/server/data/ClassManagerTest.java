package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Test;

import java.util.Date;

import static org.easymock.EasyMock.createMock;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/09/11
 */
public class ClassManagerTest {

	ClassManager cm;
	Context context;
	Dataset dataset;
	Database database;
	ClassManager.ClazzCreator builder;

	@org.junit.Before
	public void setup() {
		context = createMock(Context.class);
		database = createMock(Database.class);
		builder = createMock(ClassManager.ClazzCreator.class);

		dataset = new Dataset(new DatasetId((short) 1), "foo", new Date());

		cm = new ClassManager();
		cm.context = context;
		cm.database = database;
	}

	@Test
	public void testCreateClass() throws Exception {

	}

	@Test
	public void testUpdateClass() throws Exception {

	}

	@Test
	public void testStoreClass() throws Exception {

	}
}
