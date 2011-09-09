package nz.ac.vuw.ecs.rprofs.server.data;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;
import org.easymock.Capture;
import org.easymock.EasyMock;
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
	DB database;
	DBCollection classes;

	@org.junit.Before
	public void setup() {
		context = createMock(Context.class);
		database = createMock(DB.class);
		classes = createMock(DBCollection.class);

		dataset = new Dataset(new DataSetId((short) 1), "foo", new Date());

		cm = new ClassManager();
		cm.context = context;
	}

	@Test
	public void testCreateClass() throws Exception {
		Capture<DBObject> capture = new Capture<DBObject>();

		expect(context.getDataset()).andReturn(dataset);
		expect(context.getDB()).andReturn(database);
		expect(database.getCollection("classes")).andReturn(classes);
		expect(classes.count()).andReturn(15l);
		expect(classes.insert(EasyMock.capture(capture))).andReturn(null);

		replay(context, database, classes);

		Clazz result = cm.createClass();

		verify(context, database, classes);

		assertEquals(dataset.getId().indexValue(), result.getId().datasetValue());
		assertEquals(16, result.getId().indexValue());
		assertEquals(result.getId().longValue(), capture.getValue().get("_id"));
	}

	@Test
	public void testFindClass() throws Exception {

	}

	@Test
	public void testStoreClass() throws Exception {

	}
}
