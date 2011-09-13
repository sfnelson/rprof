package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class MongoClassBuilderTest {

	private MongoClassBuilder builder;
	private long nextId;
	private BasicDBObject storedClass;
	private BasicDBObject storedMethod;
	private BasicDBObject storedField;

	@Before
	public void setUp() throws Exception {
		builder = new MongoClassBuilder() {
			@Override
			long _nextId() {
				return nextId;
			}

			@Override
			void _storeClass(DBObject data) {
				storedClass = (BasicDBObject) data;
			}

			@Override
			void _storeField(DBObject data) {
				storedField = (BasicDBObject) data;
			}

			@Override
			void _storeMethod(DBObject data) {
				storedMethod = (BasicDBObject) data;
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
	public void testAddMethod() throws Exception {
		builder.addMethod().setAccess(1).setName("foo").setDescription("bar").store();
		assertEquals(1, builder.methods.size());
		DBObject m = builder.methods.get(0);
		assertEquals(1, m.get("access"));
		assertEquals("foo", m.get("name"));
		assertEquals("bar", m.get("description"));
	}

	@Test
	public void testAddField() throws Exception {
		builder.addField().setAccess(1).setName("foo").setDescription("bar").store();
		assertEquals(1, builder.fields.size());
		DBObject m = builder.fields.get(0);
		assertEquals(1, m.get("access"));
		assertEquals("foo", m.get("name"));
		assertEquals("bar", m.get("description"));
	}

	@Test
	public void testStore() throws Exception {
		builder.setName("org.foo.Bar");
		builder.addMethod().setAccess(1).setName("foo").setDescription("bar").store();
		builder.addField().setAccess(1).setName("foo").setDescription("bar").store();
		nextId = 15;
		builder.store();

		assertNotNull(storedClass);
		assertEquals(15l, storedClass.get("_id"));
		assertEquals("org.foo.Bar", storedClass.get("name"));

		assertNotNull(storedField);
		assertEquals(new FieldId((short) 0, 15, (short) 1).longValue(), storedField.get("_id"));
		assertEquals(15l, storedField.get("owner"));
		assertEquals("org.foo.Bar", storedField.get("ownerName"));

		assertNotNull(storedMethod);
		assertEquals(new MethodId((short) 0, 15, (short) 1).longValue(), storedMethod.get("_id"));
		assertEquals(15l, storedMethod.get("owner"));
		assertEquals("org.foo.Bar", storedMethod.get("ownerName"));

		builder.b.put("_id", 16l);
		builder.store();
		assertEquals(16l, storedClass.get("_id"));
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
		builder.b = storedClass;
		Clazz result = builder.get();

		assertEquals(3l, result.getId().longValue());
		assertEquals("org.foo.Bar", result.getName());
		assertEquals(parent, result.getParent());
		assertEquals("org.Foo", result.getParentName());
		assertEquals(2, result.getProperties());
	}

	@Test
	public void testGetField() throws Exception {
		MongoFieldBuilder fb = new MongoFieldBuilder();
		fb.b.put("_id", 2l);
		fb.b.put("name", "x");
		fb.b.put("description", "Z");
		fb.b.put("access", 15);
		fb.b.put("owner", 3l);
		fb.b.put("ownerName", "foo.Bar");
		Field f = fb.get();
		assertEquals(2l, f.getId().longValue());
		assertEquals("x", f.getName());
		assertEquals("Z", f.getDescription());
		assertEquals(15, f.getAccess());
		assertEquals(3l, f.getOwner().longValue());
		assertEquals("foo.Bar", f.getOwnerName());
	}

	@Test
	public void testGetMethod() throws Exception {
		ClazzId id = new ClazzId(1);
		Clazz cls = new Clazz(id, "foo.Bar", null, null, 0);
		MongoMethodBuilder mb = new MongoMethodBuilder();
		mb.b.put("_id", 2l);
		mb.b.put("name", "gah");
		mb.b.put("description", "()I");
		mb.b.put("access", 15);
		mb.b.put("owner", 3l);
		mb.b.put("ownerName", "foo.Bar");
		Method m = mb.get();
		assertEquals(2l, m.getId().longValue());
		assertEquals("gah", m.getName());
		assertEquals("()I", m.getDescription());
		assertEquals(15, m.getAccess());
		assertEquals(3l, m.getOwner().longValue());
		assertEquals("foo.Bar", m.getOwnerName());
	}
}
