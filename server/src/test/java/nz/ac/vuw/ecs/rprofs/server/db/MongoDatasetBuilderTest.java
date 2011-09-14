package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public class MongoDatasetBuilderTest {

	MongoDatasetBuilder builder;
	short toReturn;
	BasicDBObject stored;

	@Before
	public void setUp() throws Exception {
		builder = new MongoDatasetBuilder() {
			@Override
			public DatasetId _createId() {
				return new DatasetId(toReturn);
			}

			@Override
			public void _store(DBObject dataset) {
				stored = (BasicDBObject) dataset;
			}

			@Override
			DBCollection _getCollection() {
				throw new RuntimeException("not implemented");
			}
		};
	}

	@Test
	public void testSetHandle() throws Exception {
		builder.setHandle("foobar");
		assertEquals("foobar", builder.b.get("handle"));
	}

	@Test
	public void testSetStarted() throws Exception {
		Date date = new Date();
		builder.setStarted(date);
		assertEquals(date, builder.b.get("started"));
	}

	@Test
	public void testSetStopped() throws Exception {
		Date date = new Date();
		builder.setStopped(date);
		assertEquals(date, builder.b.get("stopped"));
	}

	@Test
	public void testSetProgram() throws Exception {
		builder.setProgram("PROG");
		assertEquals("PROG", builder.b.get("program"));
	}

	@Test
	public void testStore() throws Exception {
		toReturn = 15;
		builder.store();
		assertEquals(15l, stored.get("_id"));
	}

	@Test
	public void testGet() throws Exception {
		Date started = new Date();
		Date stopped = new Date();
		toReturn = 1;
		builder.setHandle("foobar");
		builder.setStarted(started);
		builder.setStopped(stopped);
		builder.setProgram("prog");

		builder.store();
		builder.b = stored;
		Dataset ds = builder.get();

		assertEquals(1l, ds.getId().longValue());
		assertEquals("foobar", ds.getHandle());
		assertEquals(started, ds.getStarted());
		assertEquals(stopped, ds.getStopped());
		assertEquals("prog", ds.getProgram());
	}
}
