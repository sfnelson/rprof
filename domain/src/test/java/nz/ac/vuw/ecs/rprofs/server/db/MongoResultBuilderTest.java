package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.domain.Result;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ResultId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public class MongoResultBuilderTest {

	private MongoResultBuilder builder;
	private long nextId;
	private BasicDBObject stored;
	private BasicDBObject ref;
	private BasicDBObject update;
	private BasicDBObject query;
	private DBCursor cursor;
	private long count;

	private String className = "org.Foo";
	private String packageName = "org";
	private int numObjects = 17;
	private int[] totals;
	private List<Integer> totalsList;
	private int[] counts;
	private List<Integer> countsList;

	private Map<FieldId, Result.FieldInfo> fields;

	@Before
	public void setUp() throws Exception {
		builder = new MongoResultBuilder() {
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
			ResultId _createId() {
				return new ResultId(nextId);
			}
		};

		totals = new int[]{1, 2, 3, 4};
		totalsList = Lists.newArrayList();
		for (int i : totals) totalsList.add(i);
		counts = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
				17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
		countsList = Lists.newArrayList();
		for (int i : counts) countsList.add(i);
		fields = Maps.newHashMap();
		fields.put(new FieldId(1l), new Result.FieldInfo(new FieldId(1l), 1, 2, 3));

		nextId = 1l;
	}

	@Test
	public void testInit() throws Exception {
		builder.init(new BasicDBObject("a", 1).append("b", 2));
		assertEquals(1, builder.b.get("a"));
		assertEquals(2, builder.b.get("b"));
	}

	@Test
	public void testSetTotals() throws Exception {
		builder.setTotals(totals);
		assertEquals(totalsList, builder.b.get("totals"));
	}

	@Test
	public void testSetCounts() throws Exception {
		builder.setCounts(counts);
		assertEquals(countsList, builder.b.get("counts"));
	}

	@Test
	public void testGet() throws Exception {
		builder.init(new BasicDBObjectBuilder()
				.add("_id", 1l)
				.add("class", className)
				.add("package", packageName)
				.add("objects", numObjects)
				.add("totals", totalsList)
				.add("counts", countsList)
				.add("fields", Lists.newArrayList(new BasicDBObject("_id", 1l)
						.append("mutable", 1)
						.append("reads", 2)
						.append("writes", 2)))
				.get());
		Result result = builder.get();

		assertEquals(1l, result.getId().getValue());
		assertEquals(className, result.getClassName());
		assertEquals(packageName, result.getPackageName());
		assertEquals(numObjects, result.getNumObjects());
		assertArrayEquals(totals, result.getTotals());
		assertArrayEquals(counts, result.getCounts());
		assertEquals(fields, result.getFields());
	}

	@Test
	public void testStore() throws Exception {
		nextId = 51l;
		builder.init()
				.setClassName(className)
				.setPackageName(packageName)
				.setNumObjects(numObjects)
				.setTotals(totals)
				.setCounts(counts)
				.setFields(fields)
				.store();

		assertEquals(className, stored.get("class"));
		assertEquals(packageName, stored.get("package"));
		assertEquals(numObjects, stored.get("objects"));
		assertEquals(totalsList, stored.get("totals"));
		assertEquals(countsList, stored.get("counts"));
		assertEquals(1l, ((List<DBObject>) stored.get("fields")).get(0).get("_id"));
	}
}
