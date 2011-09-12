package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.DBObject;
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
	DBObject stored;

	@Before
	public void setUp() throws Exception {
		builder = new MongoDatasetBuilder() {
			@Override
			public short _getId() {
				return toReturn;
			}

			@Override
			public void _store(DBObject dataset) {
				stored = dataset;
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
}
