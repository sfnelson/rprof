package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldSummaryId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/04/12
 */
public class MongoFieldSummaryBuilderTest {

	private MongoFieldSummaryBuilder builder;
	private long nextId;
	private BasicDBObject stored;
	private BasicDBObject ref;
	private BasicDBObject update;
	private BasicDBObject query;
	private DBCursor cursor;
	private long count;

	private String packageName = "org";
	private String name = "org.Foo.a";
	private String description = "I";
	private boolean isDeclaredFinal = true;
	private boolean isFinal = true;
	private boolean isStationary = true;
	private boolean isConstructed = true;
	private int instances = 17;
	private long reads = 42;
	private long writes = 48;

	@Before
	public void setUp() throws Exception {
		builder = new MongoFieldSummaryBuilder() {
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
			FieldSummaryId _createId() {
				return new FieldSummaryId(nextId);
			}
		};

		nextId = 1l;
	}

	@Test
	public void testInit() throws Exception {
		builder.init(new BasicDBObject("a", 1).append("b", 2));
		assertEquals(1, builder.b.get("a"));
		assertEquals(2, builder.b.get("b"));
	}

	@Test
	public void testGet() throws Exception {
		builder.init(new BasicDBObjectBuilder()
				.add("_id", 1l)
				.add("package", packageName)
				.add("name", name)
				.add("description", description)
				.add("declaredFinal", isDeclaredFinal)
				.add("final", isFinal)
				.add("stationary", isStationary)
				.add("constructed", isConstructed)
				.add("instances", instances)
				.add("reads", reads)
				.add("writes", writes)
				.get());
		FieldSummary result = builder.get();

		assertEquals(1l, result.getId().getValue());
		assertEquals(packageName, result.getPackageName());
		assertEquals(name, result.getName());
		assertEquals(description, result.getDescription());
		assertEquals(isDeclaredFinal, result.isDeclaredFinal());
		assertEquals(isFinal, result.isFinal());
		assertEquals(isStationary, result.isStationary());
		assertEquals(isConstructed, result.isConstructed());
		assertEquals(instances, result.getInstances());
		assertEquals(reads, result.getReads());
		assertEquals(writes, result.getWrites());
	}

	@Test
	public void testStore() throws Exception {
		nextId = 51l;
		builder.init()
				.setPackageName(packageName)
				.setName(name)
				.setDescription(description)
				.setDeclaredFinal(isDeclaredFinal)
				.setFinal(isFinal)
				.setStationary(isStationary)
				.setConstructed(isConstructed)
				.setInstances(instances)
				.setReads(reads)
				.setWrites(writes)
				.store();

		assertEquals(packageName, stored.get("package"));
		assertEquals(name, stored.get("name"));
		assertEquals(description, stored.get("description"));
		assertEquals(isDeclaredFinal, stored.get("declaredFinal"));
		assertEquals(isFinal, stored.get("final"));
		assertEquals(isStationary, stored.get("stationary"));
		assertEquals(isConstructed, stored.get("constructed"));
		assertEquals(instances, stored.get("instances"));
		assertEquals(reads, stored.get("reads"));
		assertEquals(writes, stored.get("writes"));
	}
}
