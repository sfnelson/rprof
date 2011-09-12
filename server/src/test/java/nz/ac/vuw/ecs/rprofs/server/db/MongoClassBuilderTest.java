package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class MongoClassBuilderTest {

	private MongoClassBuilder builder;
	private long nextId;
	private BasicDBObject stored;

	@Before
	public void setUp() throws Exception {
		builder = new MongoClassBuilder() {
			@Override
			long _nextId() {
				return nextId;
			}

			@Override
			void _store(DBObject data) {
				stored = (BasicDBObject) data;
			}
		};
	}

	@Test
	public void testInit() throws Exception {
		builder.init(new BasicDBObject("a", 1).append("b", 2));
		assertEquals(1, builder.b.get("a"));
		assertEquals(2, builder.b.get("b"));
	}

	@Test
	public void testSetName() throws Exception {
		builder.setName("foobar");
		assertEquals("foobar", builder.b.get("name"));
	}

	@Test
	public void testSetParent() throws Exception {
		ClazzId id = new ClazzId(1);
		builder.setParent(id);
		assertEquals(id.longValue(), builder.b.get("parent"));
	}

	@Test
	public void testSetParentName() throws Exception {
		builder.setParentName("foobar");
		assertEquals("foobar", builder.b.get("parentName"));
	}

	@Test
	public void testSetProperties() throws Exception {
		builder.setProperties(15);
		assertEquals(15, builder.b.get("properties"));
	}

	@Test
	public void testStore() throws Exception {
		builder.setName("org.foo.Bar");
		nextId = 15;
		builder.store();
		assertEquals(15l, stored.get("_id"));
		assertEquals("org.foo.Bar", stored.get("name"));
	}

	@Test
	public void testGet() throws Exception {
		ClazzId parent = new ClazzId(1);
		builder.setName("org.foo.Bar");
		builder.setParent(parent);
		builder.setParentName("org.Foo");
		builder.setProperties(2);
		nextId = 3l;

		builder.store();
		builder.b = stored;
		Clazz result = builder.get();

		assertEquals(3l, result.getId().longValue());
		assertEquals("org.foo.Bar", result.getName());
		assertEquals(parent, result.getParent());
		assertEquals("org.Foo", result.getParentName());
		assertEquals(2, result.getProperties());
	}
}
