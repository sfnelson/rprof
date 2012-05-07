package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.data.util.FieldQuery;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/04/12
 */
public class FieldMapReduceTest {

	Instance i;
	FieldId fid;
	Field f;
	FieldQuery<?> q;
	FieldMapReduce mr;
	Emitter<FieldSummaryId, FieldSummary> emitter;

	FieldSummaryId outId;
	FieldSummary outValue;

	@Before
	public void setup() {
		q = createMock(FieldQuery.class);
		mr = new FieldMapReduce(q);


		emitter = new Emitter<FieldSummaryId, FieldSummary>() {
			@Override
			public void store(FieldSummaryId fieldSummaryId, FieldSummary value) {
				outId = fieldSummaryId;
				outValue = value;
			}
		};
	}

	@Test
	public void testMapReduceLog() throws Exception {
		InstanceId id = new InstanceId(14636707378901932l);
		EventId constructorReturn = new EventId(14636698789177502l);
		FieldId fieldId = new FieldId(14636698824081417l);
		ClazzId classId = new ClazzId(14636698788954648l);
		f = new Field(fieldId, 1, "log", classId, "org/apache/fop/apps/FopFactoryConfigurator",
				"Lorg/apache/commons/logging/Log;", 18);
		Instance.FieldInfo info = new Instance.FieldInfo(fieldId);
		info.setReads(0);
		info.setWrites(1);
		info.setFirstWrite(new EventId(14636698789177498l));
		info.setLastWrite(new EventId(14636698789177498l));

		i = new Instance(id);
		i.setConstructorReturn(constructorReturn);
		i.addFieldInfo(fieldId, info);

		reset(q);
		expect(q.find(f.getId())).andReturn(f);
		replay(q);
		mr.map(i, emitter);
		verify(q);

		assertNotNull(outValue);
		assertEquals(fieldId.getValue(), outId.getValue());
		assertTrue(outValue.isFinal());
		assertTrue(outValue.isConstructed());
		assertTrue(outValue.isStationary());
		assertTrue(outValue.isDeclaredFinal());
		assertEquals(1, outValue.getInstances());
		assertEquals(0, outValue.getReads());
		assertEquals(1, outValue.getWrites());
	}

	@Test
	public void testMapReduceColor() throws Exception {
		fid = new FieldId(5066549676146690l);
		f = new Field(fid, 1, "color", new ClazzId(5066549580793263l),
				"org/apache/fop/fo/properties/ColorProperty", "Ljava/awt/Color", 20);

		Instance.FieldInfo info = new Instance.FieldInfo(fid);
		info.setFirstRead(new EventId(5066549581444944l));
		info.setLastRead(new EventId(5066549581444946l));
		info.setFirstWrite(new EventId(5066549581444939l));
		info.setLastWrite(new EventId(5066549581444939l));
		info.setReads(2);
		info.setWrites(1);

		i = new Instance(new InstanceId(5066558170778636l));
		i.setConstructorReturn(new EventId(5066549581444941l));
		i.addFieldInfo(fid, info);

		reset(q);
		expect(q.find(f.getId())).andReturn(f);
		replay(q);
		mr.map(i, emitter);
		verify(q);

		assertNotNull(outValue);
		assertEquals(fid.getValue(), outId.getValue());
		assertTrue(outValue.isFinal());
		assertTrue(outValue.isStationary());
		assertTrue(outValue.isConstructed());
		assertEquals(1, outValue.getInstances());
		assertEquals(2, outValue.getReads());
		assertEquals(1, outValue.getWrites());

		outValue = mr.reduce(outId, outValue, outValue);
		assertNotNull(outValue);
		assertEquals(fid.getValue(), outId.getValue());
		assertTrue(outValue.isFinal());
		assertTrue(outValue.isStationary());
		assertTrue(outValue.isConstructed());
		assertEquals(2, outValue.getInstances());
		assertEquals(4, outValue.getReads());
		assertEquals(2, outValue.getWrites());
	}
}
