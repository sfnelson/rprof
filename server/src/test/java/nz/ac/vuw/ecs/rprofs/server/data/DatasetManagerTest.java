package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetUpdater;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public class DatasetManagerTest {

	Database database;
	DatasetManager manager;

	@Before
	public void setUp() throws Exception {
		database = createMock(Database.class);
		manager = new DatasetManager(database);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateDataset() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date(), "rprof_foobar_1");

		DatasetCreator builder = createMock(DatasetCreator.class);

		expect(database.getDatasetCreator()).andReturn(builder);
		expect(builder.setBenchmark(eq("foobar"))).andReturn(builder);
		expect(builder.setStarted(anyObject(Date.class))).andReturn(builder);
		expect(builder.store()).andReturn(id);
		expect(database.findEntity(id)).andReturn(dataset);

		replay(database, builder);

		Dataset returned = manager.createDataset("foobar");

		verify(database, builder);
		assertSame(dataset, returned);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindAllDatasets() throws Exception {

		DatasetQuery query = createMock(DatasetQuery.class);

		List<? extends Dataset> datasets = Lists.newArrayList();

		expect(database.getDatasetQuery()).andReturn(query);
		expect(query.find()).andReturn(new TestCursor(datasets));

		replay(database, query);

		List<? extends Dataset> returned = manager.findAllDatasets();

		verify(database, query);
		assertEquals(returned, datasets);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindDatasetByName() throws Exception {

		DatasetQuery query = createMock(DatasetQuery.class);

		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date(), "rprof_foobar_1");
		List<Dataset> list = Lists.newArrayList(dataset);

		expect(database.getDatasetQuery()).andReturn(query);
		expect(query.setDatasetHandle("rprof_foobar_1")).andReturn(query);
		expect(query.find()).andReturn(new TestCursor(list));

		replay(database, query);

		Dataset returned = manager.findDataset("rprof_foobar_1");

		verify(database, query);
		assertEquals(dataset, returned);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindDatasetById() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date(), "rprof_foobar_1");

		expect(database.findEntity(id)).andReturn(dataset);

		replay(database);

		Dataset returned = manager.findDataset(id);

		verify(database);
		assertSame(returned, dataset);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStopDataset() throws Exception {

		DatasetUpdater b = createMock(DatasetUpdater.class);

		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date(), "rprof_foobar_1");

		expect(database.getDatasetUpdater()).andReturn(b);
		expect(b.setStopped(anyObject(Date.class))).andReturn(b);
		b.update(id);

		replay(database, b);

		manager.stopDataset(id);

		verify(database, b);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetProgram() throws Exception {

		DatasetUpdater b = createMock(DatasetUpdater.class);

		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date(), "rprof_foobar_1");

		expect(database.getDatasetUpdater()).andReturn(b);
		expect(b.setBenchmark("PROG")).andReturn(b);
		b.update(id);

		replay(database, b);

		manager.setProgram(id, "PROG");

		verify(database, b);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteDataset() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date(), "rprof_foobar_1");

		expect(database.findEntity(id)).andReturn(dataset);
		expect(database.deleteEntity(dataset)).andReturn(true);

		replay(database);

		manager.deleteDataset(id);

		verify(database);
	}

	@Test
	public void testCreate() {
		assertNotNull(manager.create(Dataset.class));
	}

	@Test
	public void testGetDomainType() {
		assertEquals(Dataset.class, manager.getDomainType());
	}

	@Test
	public void testGetIdType() {
		assertEquals(DatasetId.class, manager.getIdType());
	}

	@Test
	public void testGetId() {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date(), "rprof_foobar_1");

		assertEquals(id, manager.getId(dataset));
	}

	@Test
	public void testFind() {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date(), "rprof_foobar_1");

		expect(database.findEntity(id)).andReturn(dataset);

		replay(database);

		manager.find(Dataset.class, id);

		verify(database);
	}

	@Test
	public void testGetVersion() {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date(), "rprof_foobar_1");

		assertEquals(dataset.getVersion(), manager.getVersion(dataset));
	}
}
