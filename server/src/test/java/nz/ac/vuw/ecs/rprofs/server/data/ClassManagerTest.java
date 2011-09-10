package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;
import org.junit.Test;

import java.util.Date;

import static org.easymock.EasyMock.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/09/11
 */
public class ClassManagerTest {

	ClassManager cm;
	Context context;
	Dataset dataset;
	Database database;

	@org.junit.Before
	public void setup() {
		context = createMock(Context.class);
		database = createMock(Database.class);

		dataset = new Dataset(new DataSetId((short) 1), "foo", new Date());

		cm = new ClassManager();
		cm.context = context;
		cm.database = database;
	}

	@Test
	public void testCreateClass() throws Exception {

		expect(database.createEntity(Clazz.class)).andReturn(null);

		replay(context, database);

		Clazz result = cm.createClass();

		verify(context, database);
	}

	@Test
	public void testUpdateClass() throws Exception {
		ClassId id = ClassId.create(dataset, 1);
		Clazz cls = new Clazz(dataset, id, "org.foo.Bar", null, 0);

		expect(database.updateEntity(cls)).andReturn(cls);

		replay(context, database);

		Clazz result = cm.updateClazz(cls);

		verify(context, database);
	}

	@Test
	public void testStoreClass() throws Exception {

	}
}
