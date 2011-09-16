package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/09/11
 */
public class IdLocatorTest {

	private IdLocator locator;

	@Before
	public void setUp() {
		locator = new IdLocator();
	}

	@Test
	public void testCreate() throws Exception {
		assertNotNull(locator.create(DatasetId.class));
		assertNotNull(locator.create(ClazzId.class));
		assertNotNull(locator.create(EventId.class));
		assertNotNull(locator.create(MethodId.class));
		assertNotNull(locator.create(FieldId.class));
		assertNotNull(locator.create(InstanceId.class));
	}

	@Test
	public void testFind() throws Exception {
		assertEquals(new DatasetId(1), locator.find(DatasetId.class, 1l));
		assertEquals(new ClazzId(2), locator.find(ClazzId.class, 2l));
		assertEquals(new EventId(3), locator.find(EventId.class, 3l));
		assertEquals(new MethodId(4), locator.find(MethodId.class, 4l));
		assertEquals(new FieldId(5), locator.find(FieldId.class, 5l));
		assertEquals(new InstanceId(6), locator.find(InstanceId.class, 6l));
	}

	@Test
	public void testGetDomainType() throws Exception {
		assertEquals(Id.class, locator.getDomainType());
	}

	@Test
	public void testGetId() throws Exception {
		assertEquals(1l, locator.getId(new DatasetId(1)).longValue());
		assertEquals(2l, locator.getId(new ClazzId(2)).longValue());
		assertEquals(3l, locator.getId(new EventId(3)).longValue());
		assertEquals(4l, locator.getId(new MethodId(4)).longValue());
		assertEquals(5l, locator.getId(new FieldId(5)).longValue());
		assertEquals(6l, locator.getId(new InstanceId(6)).longValue());
	}

	@Test
	public void testGetIdType() throws Exception {
		assertEquals(Long.class, locator.getIdType());
	}

	@Test
	public void testGetVersion() throws Exception {
		assertEquals(1, locator.getVersion(new DatasetId(1)));
	}
}
