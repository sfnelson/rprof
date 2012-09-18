package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
		builder.setBenchmark("foobar");
		assertEquals("foobar", builder.b.get("benchmark"));
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
		builder.setBenchmark("PROG");
		assertEquals("PROG", builder.b.get("benchmark"));
	}

	@Test
	public void testStore() throws Exception {
		toReturn = 15;
		DatasetId id = builder.store();
		Assert.assertEquals(15l, id.getValue());
		Assert.assertEquals(15l, stored.get("_id"));
	}

	@Test
	public void testGet() throws Exception {
		Date started = new Date();
		Date stopped = new Date();

		builder.init(new BasicDBObjectBuilder()
				.add("_id", 1l)
				.add("benchmark", "foobar")
				.add("started", started)
				.add("stopped", stopped)
				.add("handle", "rprof_foobar_1")
				.get());
		Dataset ds = builder.get();

		Assert.assertEquals(1l, ds.getId().getValue());
		Assert.assertEquals("foobar", ds.getBenchmark());
		Assert.assertEquals(started, ds.getStarted());
		Assert.assertEquals(stopped, ds.getStopped());
		Assert.assertEquals("rprof_foobar_1", ds.getDatasetHandle());
	}
}
