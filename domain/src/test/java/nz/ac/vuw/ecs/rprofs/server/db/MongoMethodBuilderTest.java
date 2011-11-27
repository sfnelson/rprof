package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public class MongoMethodBuilderTest {

	private MongoClassBuilder cb;
	private MongoMethodBuilder mb;

	private MethodId nextId;

	private List<BasicDBObject> stored = Lists.newArrayList();

	@Before
	public void setUp() throws Exception {
		stored.clear();
		nextId = null;

		cb = createMock(MongoClassBuilder.class);
		mb = new MongoMethodBuilder(cb) {
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
			MethodId _createId() {
				return nextId;
			}
		};
	}

	@Test
	public void testSetName() throws Exception {
		mb.setName("foobar");
		assertEquals("foobar", mb.b.get("name"));
	}

	@Test
	public void testSetDescription() throws Exception {
		mb.setDescription("foobar");
		assertEquals("foobar", mb.b.get("description"));
	}

	@Test
	public void testSetAccess() throws Exception {
		mb.setAccess(15);
		assertEquals(15, mb.b.get("access"));
	}

	@Test
	public void testSetOwner() throws Exception {
		ClazzId id = new ClazzId(15);
		mb.setOwner(id);
		assertEquals(id.getValue(), mb.b.get("owner"));
	}

	@Test
	public void testSetOwnerName() throws Exception {
		mb.setOwnerName("foobar");
		assertEquals("foobar", mb.b.get("ownerName"));
	}

	@Test
	public void testStore() throws Exception {
		ClazzId id = new ClazzId(15l);
		this.nextId = new MethodId(16l);

		cb.addMethod(mb);

		replay(cb);

		mb.store();

		verify(cb);

		reset(cb);

		replay(cb);

		mb.store(id, "org.foo.Bar");

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
		MethodId methodId = new MethodId(16l);
		String name = "fuzz";
		String desc = "fozz";
		int access = 15;
		ClazzId clazzId = new ClazzId(17l);
		String cname = "org/foo/Bar";

		mb.init(new BasicDBObjectBuilder()
				.add("_id", methodId.getValue())
				.add("name", name)
				.add("description", desc)
				.add("access", access)
				.add("owner", clazzId.getValue())
				.add("ownerName", cname)
				.get());

		Method m = mb.get();

		assertEquals(methodId, m.getId());
		assertEquals(name, m.getName());
		assertEquals(desc, m.getDescription());
		assertEquals(access, m.getAccess());
		assertEquals(clazzId, m.getOwner());
		assertEquals(cname, m.getOwnerName());
	}
}
