package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public class MongoFieldBuilderTest {

	private MongoClassBuilder cb;
	private MongoFieldBuilder fb;

	private FieldId nextId;

	private List<BasicDBObject> stored = Lists.newArrayList();

	@Before
	public void setUp() throws Exception {
		stored.clear();
		nextId = null;

		cb = createMock(MongoClassBuilder.class);
		fb = new MongoFieldBuilder(cb) {
			@Override
			void _store(DBObject toStore) {
				BasicDBObject s = new BasicDBObject();
				s.putAll(toStore);
				stored.add(s);
			}

			@Override
			void _update(DBObject ref, DBObject update) {
				throw new RuntimeException("not implemented");
			}

			@Override
			DBCursor _query(DBObject query) {
				throw new RuntimeException("not implemented");
			}

			@Override
			long _count(DBObject query) {
				throw new RuntimeException("not implemented");
			}

			@Override
			DBCollection _getCollection() {
				throw new RuntimeException("not implemented");
			}

			@Override
			FieldId _createId() {
				return nextId;
			}
		};
	}

	@Test
	public void testSetName() throws Exception {
		fb.setName("foobar");
		assertEquals("foobar", fb.b.get("name"));
	}

	@Test
	public void testSetDescription() throws Exception {
		fb.setDescription("foobar");
		assertEquals("foobar", fb.b.get("description"));
	}

	@Test
	public void testSetAccess() throws Exception {
		fb.setAccess(15);
		assertEquals(15, fb.b.get("access"));
	}

	@Test
	public void testSetOwner() throws Exception {
		ClazzId id = new ClazzId(15);
		fb.setOwner(id);
		assertEquals(id.getValue(), fb.b.get("owner"));
	}

	@Test
	public void testSetOwnerName() throws Exception {
		fb.setOwnerName("foobar");
		assertEquals("foobar", fb.b.get("ownerName"));
	}

	@Test
	public void testStore() throws Exception {
		ClazzId id = new ClazzId(15l);
		this.nextId = new FieldId(16l);

		cb.addField(fb);

		replay(cb);

		fb.store();

		verify(cb);

		reset(cb);

		replay(cb);

		fb.store(id, "org.foo.Bar");

		verify(cb);

		assertEquals(1, stored.size());
		BasicDBObject o = stored.get(0);
		assertEquals(new BasicDBObjectBuilder()
				.add("_id", 16l)
				.add("owner", 15l)
				.add("ownerName", "org.foo.Bar")
				.get(),
				stored.get(0));
	}

	@Test
	public void testGet() throws Exception {
		FieldId fieldId = new FieldId(16l);
		String name = "fuzz";
		String desc = "fozz";
		int access = 15;
		ClazzId clazzId = new ClazzId(17l);
		String cname = "org/foo/Bar";

		fb.init(new BasicDBObjectBuilder()
				.add("_id", fieldId.getValue())
				.add("name", name)
				.add("description", desc)
				.add("access", access)
				.add("owner", clazzId.getValue())
				.add("ownerName", cname)
				.get());

		Field f = fb.get();

		assertEquals(fieldId, f.getId());
		assertEquals(name, f.getName());
		assertEquals(desc, f.getDescription());
		assertEquals(access, f.getAccess());
		assertEquals(clazzId, f.getOwner());
		assertEquals(cname, f.getOwnerName());
	}
}
