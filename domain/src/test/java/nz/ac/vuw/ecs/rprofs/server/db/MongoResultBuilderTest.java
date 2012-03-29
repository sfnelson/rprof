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
	private int[] eqcol;
	private List<Integer> eqcolList;
	private int[] eq;
	private List<Integer> eqList;
	private int[] col;
	private List<Integer> colList;
	private int[] none;
	private List<Integer> noneList;

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

		eqcol = new int[32];
		eq = new int[32];
		col = new int[32];
		none = new int[32];
		for (int i = 0; i < 32; i++) eq[i] = i;
		for (int i = 0; i < 32; i++) eq[i] = i + 32;
		for (int i = 0; i < 32; i++) eq[i] = i + 64;
		for (int i = 0; i < 32; i++) eq[i] = i + 96;

		eqcolList = Lists.newArrayList();
		for (int i : eqcol) eqcolList.add(i);
		eqList = Lists.newArrayList();
		for (int i : eq) eqList.add(i);
		colList = Lists.newArrayList();
		for (int i : col) colList.add(i);
		noneList = Lists.newArrayList();
		for (int i : none) noneList.add(i);

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
	public void testGet() throws Exception {
		builder.init(new BasicDBObjectBuilder()
				.add("_id", 1l)
				.add("class", className)
				.add("package", packageName)
				.add("objects", numObjects)
				.add("eqcol", eqcolList)
				.add("eq", eqList)
				.add("col", colList)
				.add("none", noneList)
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
		assertArrayEquals(eqcol, result.getEqCol());
		assertArrayEquals(eq, result.getEq());
		assertArrayEquals(col, result.getCol());
		assertArrayEquals(none, result.getNone());
		assertEquals(fields, result.getFields());
	}

	@Test
	public void testStore() throws Exception {
		nextId = 51l;
		builder.init()
				.setClassName(className)
				.setPackageName(packageName)
				.setNumObjects(numObjects)
				.setEqCol(eqcol)
				.setEq(eq)
				.setCol(col)
				.setNone(none)
				.setFields(fields)
				.store();

		assertEquals(className, stored.get("class"));
		assertEquals(packageName, stored.get("package"));
		assertEquals(numObjects, stored.get("objects"));
		assertEquals(eqcolList, stored.get("eqcol"));
		assertEquals(eqList, stored.get("eq"));
		assertEquals(colList, stored.get("col"));
		assertEquals(noneList, stored.get("none"));
		assertEquals(1l, ((List<DBObject>) stored.get("fields")).get(0).get("_id"));
	}
}
