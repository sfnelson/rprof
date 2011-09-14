package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
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
	long toReturn;
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
				stored = new BasicDBObject();
				stored.putAll(dataset);
			}

			@Override
			DBCollection _getCollection() {
				throw new RuntimeException("not implemented");
			}
		};

		toReturn = 0;
		stored = null;
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
		DatasetId id = builder.store();
		assertEquals(15l, id.longValue());
		assertEquals(15l, stored.get("_id"));
	}

	@Test
	public void testGet() throws Exception {
		Date started = new Date();
		Date stopped = new Date();

		builder.init(new BasicDBObjectBuilder()
				.add("_id", 1l)
				.add("handle", "foobar")
				.add("started", started)
				.add("stopped", stopped)
				.add("program", "prog")
				.get());
		Dataset ds = builder.get();

		assertEquals(1l, ds.getId().longValue());
		assertEquals("foobar", ds.getHandle());
		assertEquals(started, ds.getStarted());
		assertEquals(stopped, ds.getStopped());
		assertEquals("prog", ds.getProgram());
	}
}
