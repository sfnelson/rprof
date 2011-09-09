package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.data.EventManager;
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

	@Before
	public void createBuilder() {
		b = new MongoEventBuilder();
		ds = new Dataset(new DataSetId(DS), "foo", new Date());
		b.setDataSet(ds);
	}

	@Test
	public void testDataSet() throws Exception {
		assertEquals(ds, b.getDataset());
	}

	@Test
	public void testSetId() throws Exception {
		assertNull(b.getId());
		assertEquals(b, b.setId(15));
		assertEquals(DS, b.getId().datasetValue());
		assertEquals(15, b.getId().eventValue());
	}

	@Test
	public void testSetThread() throws Exception {
		assertNull(b.getThread());
		assertEquals(b, b.setThread(15));
		assertEquals(DS, b.getThread().datasetValue());
		assertEquals(15, b.getThread().indexValue());
	}

	@Test
	public void testSetEvent() throws Exception {
		assertNull(b.getEvent());
		assertEquals(b, b.setEvent(15));
		assertEquals(15, b.getEvent().longValue());
	}

	@Test
	public void testSetClazz() throws Exception {
		assertNull(b.getClazz());
		assertEquals(b, b.setClazz(15));
		assertEquals(DS, b.getClazz().datasetValue());
		assertEquals(15, b.getClazz().indexValue());
	}

	@Test
	public void testSetMethod() throws Exception {
		assertNull(b.getMethod());
		assertEquals(b, b.setClazz(15));
		assertEquals(b, b.setMethod((short) 35));
		assertEquals(DS, b.getMethod().datasetValue());
		assertEquals(15, b.getMethod().typeValue());
		assertEquals(35, b.getMethod().attributeValue());
	}

	@Test
	public void testSetField() throws Exception {
		assertNull(b.getField());
		assertEquals(b, b.setClazz(15));
		assertEquals(b, b.setField((short) 35));
		assertEquals(DS, b.getField().datasetValue());
		assertEquals(15, b.getField().typeValue());
		assertEquals(35, b.getField().attributeValue());
	}

	@Test
	public void testArgs() throws Exception {
		assertTrue(b.getArgs().isEmpty());

		assertEquals(b, b.addArg(1));
		assertEquals(1, b.getArgs().size());
		assertEquals(DS, b.getArgs().get(0).datasetValue());
		assertEquals(1, b.getArgs().get(0).indexValue());

		assertEquals(b, b.addArg(2));
		assertEquals(2, b.getArgs().size());
		assertEquals(DS, b.getArgs().get(1).datasetValue());
		assertEquals(2, b.getArgs().get(1).indexValue());

		assertEquals(b, b.clearArgs());
		assertTrue(b.getArgs().isEmpty());
	}

	@Test
	public void testToObject() throws Exception {
		assertEquals(EventId.create(ds, 1).longValue(),
				m(b.setId(1)).toDBObject().get("_id"));

		assertNull(b.toDBObject().get("thread"));
		assertEquals(ObjectId.create(ds, 1).longValue(),
				m(b.setThread(1)).toDBObject().get("thread"));

		assertEquals(2, m(b.setEvent(2)).toDBObject().get("event"));

		assertNull(b.toDBObject().get("class"));
		assertEquals(ClassId.create(ds, 3).longValue(),
				m(b.setClazz(3)).toDBObject().get("class"));

		assertNull(b.toDBObject().get("method"));
		assertEquals(MethodId.create(ds, ClassId.create(ds, 3), (short) 4).longValue(),
				m(b.setMethod((short) 4)).toDBObject().get("method"));

		assertNull(b.toDBObject().get("field"));
		assertEquals(FieldId.create(ds, ClassId.create(ds, 3), (short) 5).longValue(),
				m(b.setField((short) 5)).toDBObject().get("field"));

		assertNull(b.toDBObject().get("args"));
		assertEquals(Lists.newArrayList(ObjectId.create(ds, 1).longValue(), ObjectId.create(ds, 2).longValue()),
				m(b.addArg(1).addArg(2)).toDBObject().get("args"));
	}

	private MongoEventBuilder m(EventManager.EventBuilder b) {
		return (MongoEventBuilder) b;
	}
}
