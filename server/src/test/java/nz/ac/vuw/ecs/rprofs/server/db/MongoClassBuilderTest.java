package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class MongoClassBuilderTest {

	private MongoClassBuilder builder;
	private long nextId;
	private BasicDBObject stored;
	private BasicDBObject ref;
	private BasicDBObject update;
	private BasicDBObject query;
	private DBCursor cursor;
	private long count;

	private MongoFieldBuilder fBuilder;
	private MongoMethodBuilder mBuilder;

	@Before
	public void setUp() throws Exception {
		builder = new MongoClassBuilder() {
			@Override
			void _store(DBObject toStore) {
				stored = new BasicDBObject();
				stored.putAll(toStore);
			}

			@Override
			void _update(DBObject _ref, DBObject _update) {
				ref = (BasicDBObject) _ref;
				update = (BasicDBObject) _update;
			}

			@Override
			DBCursor _query(DBObject _query) {
				query = (BasicDBObject) _query;
				return cursor;
			}

			@Override
			long _count(DBObject _query) {
				query = (BasicDBObject) _query;
				return count;
			}

			@Override
			public ClassManager.FieldCreator addField() {
				return fBuilder;
			}

			@Override
			public ClassManager.MethodCreator addMethod() {
				return mBuilder;
			}

			@Override
			DBCollection _getCollection() {
				throw new RuntimeException("not implemented");
			}

			@Override
			ClazzId _createId() {
				return new ClazzId(nextId);
			}
		};

		nextId = 0;
		stored = null;
		ref = null;
		update = null;
		query = null;
		long count = 0;

		cursor = createMock(DBCursor.class);
		mBuilder = createMock(MongoMethodBuilder.class);
		fBuilder = createMock(MongoFieldBuilder.class);
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

		ClazzId id = new ClazzId(15l);

		expect(mBuilder.store(id, "org.foo.Bar")).andReturn(null);
		expect(fBuilder.store(id, "org.foo.Bar")).andReturn(null);
		expect(cursor.hasNext()).andReturn(false); // get children
		cursor.close();

		replay(fBuilder, mBuilder, cursor);

		builder.setName("org.foo.Bar");
		builder.addMethod(mBuilder);
		builder.addField(fBuilder);
		nextId = 15;
		builder.store();

		verify(fBuilder, mBuilder, cursor);

		assertNotNull(stored);
		assertEquals(15l, stored.get("_id"));
		assertEquals("org.foo.Bar", stored.get("name"));
		assertEquals(new BasicDBObjectBuilder().add("parentName", "org.foo.Bar").get(), query);
	}

	@Test
	public void testGet() throws Exception {
		builder.init(new BasicDBObjectBuilder()
				.add("_id", 1l)
				.add("name", "org.foo.Bar")
				.add("parent", 2l)
				.add("parentName", "org.Foo")
				.add("properties", 3).get());
		Clazz result = builder.get();

		assertEquals(1l, result.getId().longValue());
		assertEquals("org.foo.Bar", result.getName());
		assertEquals(new ClazzId(2l), result.getParent());
		assertEquals("org.Foo", result.getParentName());
		assertEquals(3, result.getProperties());
	}
}
