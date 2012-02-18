package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/12/11
 */
public class MongoInstanceBuilderTest {

	private MongoInstanceBuilder builder;
	private long nextId;
	private BasicDBObject stored;
	private BasicDBObject ref;
	private BasicDBObject update;
	private BasicDBObject query;
	private DBCursor cursor;
	private long count;

	private ClazzId clazzId;
	private MethodId constructor;
	private EventId consReturn;
	private EventId firstEquals;
	private EventId firstHashcode;
	private EventId firstCollection;

	@Before
	public void setUp() throws Exception {
		builder = new MongoInstanceBuilder() {
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
			DBCollection _getCollection() {
				throw new RuntimeException("not implemented");
			}

			@Override
			InstanceId _createId() {
				return new InstanceId(nextId);
			}
		};

		clazzId = new ClazzId((short) 33, 310);
		constructor = new MethodId((short) 33, 310, (short) 2);
		consReturn = new EventId((short) 33, 50123);
		firstEquals = new EventId((short) 33, 50132);
		firstHashcode = new EventId((short) 33, 50131);
		firstCollection = new EventId((short) 33, 50133);

		nextId = 1l;
	}

	@Test
	public void testInit() throws Exception {
		builder.init(new BasicDBObject("a", 1).append("b", 2));
		assertEquals(1, builder.b.get("a"));
		assertEquals(2, builder.b.get("b"));
	}

	@Test
	public void testSetType() throws Exception {
		builder.setType(clazzId);
		assertEquals(clazzId.getValue(), builder.b.get("type"));
	}

	@Test
	public void testSetConstructor() throws Exception {
		builder.setConstructor(constructor);
		assertEquals(constructor.getValue(), builder.b.get("constructor"));
	}

	@Test
	public void testSetConstructorReturn() throws Exception {
		builder.setConstructorReturn(consReturn);
		assertEquals(consReturn.getValue(), builder.b.get("constructorReturn"));
	}

	@Test
	public void testSetFirstEquals() throws Exception {
		builder.setFirstEquals(firstEquals);
		assertEquals(firstEquals.getValue(), builder.b.get("firstEquals"));
	}

	@Test
	public void testSetFirstHashCode() throws Exception {
		builder.setFirstHashCode(firstHashcode);
		assertEquals(firstHashcode.getValue(), builder.b.get("firstHashCode"));
	}

	@Test
	public void testSetFirstCollection() throws Exception {
		builder.setFirstCollection(firstCollection);
		assertEquals(firstCollection.getValue(), builder.b.get("firstCollection"));
	}

	@Test
	public void testGet() throws Exception {
		builder.init(new BasicDBObjectBuilder()
				.add("_id", 1l)
				.add("type", clazzId.getValue())
				.add("constructor", constructor.getValue())
				.add("constructorReturn", consReturn.getValue())
				.add("firstEquals", firstEquals.getValue())
				.add("firstHashCode", firstHashcode.getValue())
				.add("firstCollection", firstCollection.getValue())
				.get());
		Instance result = builder.get();

		assertEquals(1l, result.getId().getValue());
		assertEquals(clazzId, result.getType());
		assertEquals(constructor, result.getConstructor());
		assertEquals(consReturn, result.getConstructorReturn());
		assertEquals(firstEquals, result.getFirstEquals());
		assertEquals(firstHashcode, result.getFirstHashCode());
		assertEquals(firstCollection, result.getFirstCollection());
	}

	@Test
	public void testStore() throws Exception {
		nextId = 51l;
		builder.init()
				.setType(clazzId)
				.setConstructor(constructor)
				.setConstructorReturn(consReturn)
				.setFirstEquals(firstEquals)
				.setFirstHashCode(firstHashcode)
				.setFirstCollection(firstCollection)
				.store();

		assertEquals(clazzId.getValue(), stored.get("type"));
		assertEquals(constructor.getValue(), stored.get("constructor"));
		assertEquals(consReturn.getValue(), stored.get("constructorReturn"));
		assertEquals(firstEquals.getValue(), stored.get("firstEquals"));
		assertEquals(firstHashcode.getValue(), stored.get("firstHashCode"));
		assertEquals(firstCollection.getValue(), stored.get("firstCollection"));
	}
}
