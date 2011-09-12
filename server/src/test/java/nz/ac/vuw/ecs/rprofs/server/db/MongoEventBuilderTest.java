package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/09/11
 */
public class MongoEventBuilderTest {

	private static final short DS = 8;

	private Dataset ds;
	private MongoEventBuilder b;

	private DBObject result;

	@Before
	public void createBuilder() {
		ds = new Dataset(new DatasetId(DS), "foo", new Date());
		b = new MongoEventBuilder() {
			void _store(DBObject toStore) {
				result = toStore;
			}
		};
	}

	@Test
	public void testSetId() throws Exception {
		EventId id = EventId.create(ds, 1);

		assertNull(b.b.get("_id"));
		assertSame(b, b.setId(id));
		assertEquals(id.longValue(), b.b.get("_id"));
	}

	@Test
	public void testSetThread() throws Exception {
		InstanceId thread = InstanceId.create(ds, 2);

		assertNull(b.b.get("thread"));
		assertEquals(b, b.setThread(thread));
		assertEquals(thread.longValue(), b.b.get("thread"));
	}

	@Test
	public void testSetEvent() throws Exception {
		assertNull(b.b.get("event"));
		assertEquals(b, b.setEvent(3));
		assertEquals(3, b.b.get("event"));
	}

	@Test
	public void testSetClazz() throws Exception {
		ClazzId clazz = ClazzId.create(ds, 4);

		assertNull(b.b.get("class"));
		assertEquals(b, b.setClazz(clazz));
		assertEquals(clazz.longValue(), b.b.get("class"));
	}

	@Test
	public void testSetMethod() throws Exception {
		ClazzId clazz = ClazzId.create(ds, 4);
		MethodId method = MethodId.create(ds, clazz, (short) 5);

		assertNull(b.b.get("method"));
		assertEquals(b, b.setMethod(method));
		assertEquals(method.longValue(), b.b.get("method"));
	}

	@Test
	public void testSetField() throws Exception {
		ClazzId clazz = ClazzId.create(ds, 4);
		FieldId field = FieldId.create(ds, clazz, (short) 6);

		assertNull(b.b.get("field"));
		assertEquals(b, b.setField(field));
		assertEquals(field.longValue(), b.b.get("field"));
	}

	@Test
	public void testArgs() throws Exception {
		InstanceId x = InstanceId.create(ds, 7);
		InstanceId y = null;
		InstanceId z = InstanceId.create(ds, 8);

		assertTrue(b.args.isEmpty());

		b.addArg(x);
		b.addArg(y);
		b.addArg(z);

		assertEquals(Lists.newArrayList(x.longValue(), null, z.longValue()), b.args);
	}

	@Test
	public void testStore() throws Exception {
		EventId id = EventId.create(ds, 1);
		InstanceId thread = InstanceId.create(ds, 2);
		ClazzId clazz = ClazzId.create(ds, 4);
		MethodId method = MethodId.create(ds, clazz, (short) 5);
		FieldId field = FieldId.create(ds, clazz, (short) 6);
		InstanceId x = InstanceId.create(ds, 7);
		InstanceId y = null;
		InstanceId z = InstanceId.create(ds, 8);

		b.setId(id);
		b.setThread(thread);
		b.setEvent(3);
		b.setClazz(clazz);
		b.setMethod(method);
		b.setField(field);
		b.addArg(x);
		b.addArg(y);
		b.addArg(z);

		b.store();

		assertTrue(b.b.isEmpty());

		assertEquals(id.longValue(), result.get("_id"));
		assertEquals(thread.longValue(), result.get("thread"));
		assertEquals(3, result.get("event"));
		assertEquals(clazz.longValue(), result.get("class"));
		assertEquals(method.longValue(), result.get("method"));
		assertEquals(field.longValue(), result.get("field"));
		assertEquals(Lists.newArrayList(x.longValue(), null, z.longValue()), result.get("args"));
	}
}
