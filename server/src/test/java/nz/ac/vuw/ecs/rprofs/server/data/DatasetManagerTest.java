package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetUpdater;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertSame;

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
		manager = new DatasetManager();
		manager.database = database;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateDataset() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date());

		DatasetCreator builder = createMock(DatasetCreator.class);

		expect(database.getDatasetCreator()).andReturn(builder);
		expect(builder.setHandle(anyObject(String.class))).andReturn(builder);
		expect(builder.setStarted(anyObject(Date.class))).andReturn(builder);
		expect(builder.store()).andReturn(id);
		expect(database.findEntity(id)).andReturn(dataset);

		replay(database, builder);

		Dataset returned = manager.createDataset();

		verify(database, builder);
		assertSame(dataset, returned);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindAllDatasets() throws Exception {

		DatasetQuery query = createMock(DatasetQuery.class);

		List<? extends Dataset> datasets = Lists.newArrayList();

		expect(database.getDatasetQuery()).andReturn(query);
		expect(query.find()).andReturn(datasets);

		replay(database, query);

		List<? extends Dataset> returned = manager.findAllDatasets();

		verify(database, query);
		assertSame(returned, datasets);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindDatasetByName() throws Exception {

		DatasetQuery query = createMock(DatasetQuery.class);

		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date());
		List<Dataset> list = Lists.newArrayList(dataset);

		expect(database.getDatasetQuery()).andReturn(query);
		expect(query.setHandle("foobar")).andReturn(query);
		expect(query.find()).andReturn(list);

		replay(database, query);

		Dataset returned = manager.findDataset("foobar");

		verify(database, query);
		assertSame(dataset, returned);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindDatasetById() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date());

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
		Dataset dataset = new Dataset(id, "foobar", new Date());

		expect(database.getDatasetUpdater()).andReturn(b);
		expect(b.setStopped(anyObject(Date.class))).andReturn(b);
		b.update(id);

		replay(database, b);

		manager.stopDataset(dataset);

		verify(database, b);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetProgram() throws Exception {

		DatasetUpdater b = createMock(DatasetUpdater.class);

		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date());

		expect(database.getDatasetUpdater()).andReturn(b);
		expect(b.setProgram("PROG")).andReturn(b);
		b.update(id);

		replay(database, b);

		manager.setProgram(dataset, "PROG");

		verify(database, b);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteDataset() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date());

		expect(database.deleteEntity(dataset)).andReturn(true);

		replay(database);

		manager.deleteDataset(dataset);

		verify(database);
	}
}
