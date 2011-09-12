package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.collect.Lists;
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

	@Test
	public void testCreateDataset() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date());

		DatasetManager.DatasetBuilder builder = createMock(DatasetManager.DatasetBuilder.class);

		expect(database.getDatasetBuilder()).andReturn(builder);
		expect(builder.setHandle(anyObject(String.class))).andReturn(builder);
		expect(builder.setStarted(anyObject(Date.class))).andReturn(builder);
		expect(builder.store()).andReturn(id);
		expect(database.findEntity(id)).andReturn(dataset);

		replay(database, builder);

		Dataset returned = manager.createDataset();

		verify(database, builder);
		assertSame(dataset, returned);
	}

	@Test
	public void testFindAllDatasets() throws Exception {

		List<Dataset> datasets = Lists.newArrayList();

		expect(database.findEntities(Dataset.class)).andReturn(datasets);

		replay(database);

		List<Dataset> returned = manager.findAllDatasets();

		verify(database);
		assertSame(returned, datasets);
	}

	@Test
	public void testFindDataset() throws Exception {

		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date());
		List<Dataset> list = Lists.newArrayList(dataset);

		expect(database.findEntities(Dataset.class, "foobar")).andReturn(list);

		replay(database);

		Dataset returned = manager.findDataset("foobar");

		verify(database);
		assertSame(dataset, returned);
	}

	@Test
	public void testStopDataset() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date());

		DatasetManager.DatasetBuilder builder = createMock(DatasetManager.DatasetBuilder.class);
		expect(database.getDatasetUpdater(dataset)).andReturn(builder);
		expect(builder.setStopped(anyObject(Date.class))).andReturn(builder);
		expect(builder.store()).andReturn(id);

		replay(database, builder);

		manager.stopDataset(dataset);

		verify(database, builder);
	}

	@Test
	public void testSetProgram() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "foobar", new Date());

		DatasetManager.DatasetBuilder builder = createMock(DatasetManager.DatasetBuilder.class);
		expect(database.getDatasetUpdater(dataset)).andReturn(builder);
		expect(builder.setProgram("PROG")).andReturn(builder);
		expect(builder.store()).andReturn(id);

		replay(database, builder);

		manager.setProgram(dataset, "PROG");

		verify(database, builder);
	}

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
