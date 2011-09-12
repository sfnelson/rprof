package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.mongodb.Mongo;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.*;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/09/11
 */
public class DatabaseIntegrationTest {

	private static final org.slf4j.Logger log
			= LoggerFactory.getLogger(DatabaseIntegrationTest.class);

	private static Process process;
	private static Thread sysout;
	private static Thread syserr;

	@BeforeClass
	public static void createDatabase() throws Exception {
		ProcessBuilder pb = new ProcessBuilder();
		pb.environment().put("PATH", "/bin:/usr/bin:/usr/local/bin:~/bin");
		pb.command("/usr/bin/which", "mongod");
		Scanner in = new Scanner(pb.start().getInputStream());
		Assert.assertTrue(in.hasNextLine());
		String path = in.nextLine();

		pb.command().clear();
		pb.command("/bin/mkdir", "-p", "/tmp/rprof/mongo");
		pb.start();

		pb.command().clear();
		pb.command(path, "--dbpath", "/tmp/rprof/mongo",
				"--bind_ip", "127.0.0.1", "--port", "27018", "--logpath", "/tmp/rprof/mongod.log");
		process = pb.start();

		sysout = new Thread() {
			public void run() {
				for (Scanner sc = new Scanner(process.getInputStream()); sc.hasNextLine(); ) {
					log.info(sc.nextLine());
				}
			}
		};
		syserr = new Thread() {
			public void run() {
				for (Scanner sc = new Scanner(process.getErrorStream()); sc.hasNextLine(); ) {
					log.error(sc.nextLine());
				}
			}
		};

		sysout.start();
		syserr.start();

		Thread.sleep(2000);
	}

	private Database database;
	private Mongo mongo;
	private DatasetManager.DatasetBuilder builder;

	@Before
	public void createMongo() throws Exception {
		mongo = new Mongo("127.0.0.1", 27018);
		database = new Database(mongo);
		builder = database.getDatasetBuilder();
	}

	@Test
	public void testCreateDataset() throws Exception {
		List<String> dbs = mongo.getDatabaseNames();
		dbs.remove("admin");
		dbs.remove("local");
		dbs.remove("test");
		assertEquals(Lists.<String>newArrayList(), dbs);

		DatasetId id = database.getDatasetBuilder()
				.setHandle("foobar")
				.setStarted(new Date())
				.store();
		Dataset dataset = database.findEntity(id);

		assertNotNull(dataset);
		assertNotNull(id);
		assertEquals(id, dataset.getId());
		assertTrue(id.indexValue() > 0);
		assertNotNull(dataset.getHandle());
		assertFalse(dataset.getHandle().isEmpty());
		assertNotNull(dataset.getId());
		assertEquals(dataset.getId().indexValue(), dataset.getId().indexValue());

		dbs = mongo.getDatabaseNames();
		dbs.remove("admin");
		dbs.remove("local");
		dbs.remove("test");
		assertEquals(1, dbs.size());

		String dbname = dbs.get(0);
		assertEquals(dbname, database.getDBName(dataset));
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals("rprof_foo_0", database.getDBName(new Dataset(new DatasetId((short) 0), "foo", new Date())));
		assertEquals("rprof_20110101_1", database.getDBName(new Dataset(new DatasetId((short) 1), "20110101", new Date())));
	}

	@Test
	public void testGetNextId() throws Exception {
		assertEquals((short) 1, database.getNextId());
		builder.setHandle("foo").setStarted(new Date()).store();
		assertEquals((short) 2, database.getNextId());
		builder.setHandle("bar").setStarted(new Date()).store();
		assertEquals((short) 3, database.getNextId());
	}

	@Test
	public void testGetDatasets() throws Exception {
		assertEquals(new ArrayList<Dataset>(), database.findEntities(Dataset.class));
		DatasetId id1 = builder.setHandle("foo").setStarted(new Date()).store();
		assertEquals(id1, database.findEntities(Dataset.class).get(0).getId());
		DatasetId id2 = builder.setHandle("foo").setStarted(new Date()).store();

		List<Dataset> result = database.findEntities(Dataset.class);
		Collections.sort(result);
		assertEquals(id1, result.get(0).getId());
		assertEquals(id2, result.get(1).getId());
	}

	@Test
	public void testGetDatasetDatasetId() throws Exception {
		DatasetId in = builder.setHandle("foobar").setStarted(new Date()).store();
		assertEquals("foobar", database.findEntity(in).getHandle());

		assertNull(database.findEntity(new DatasetId((short) 0)));
	}

	@Test
	public void testDropDataset() throws Exception {
		DatasetId in = builder.setHandle("foobar").setStarted(new Date()).store();
		List<Dataset> datasets = database.findEntities(Dataset.class);
		assertEquals(1, datasets.size());
		assertEquals(in, datasets.get(0).getId());
		database.deleteEntity(database.findEntity(in));
		assertEquals(new ArrayList<Dataset>(), database.findEntities(Dataset.class));

		assertNull(database.findEntity(in));
	}

	@After
	public void destroyMongo() throws Exception {
		for (String db : mongo.getDatabaseNames()) {
			if (db.equals("test")) continue;
			if (db.equals("admin")) continue;
			if (db.equals("local")) continue;
			mongo.dropDatabase(db);
		}
		mongo.close();
	}

	@AfterClass
	public static void destroyDatabase() throws Exception {
		process.destroy();
		sysout.join();
		syserr.join();

		ProcessBuilder pb = new ProcessBuilder();
		pb.command("/bin/rm", "-rf", "/tmp/rprof");
		pb.start();
	}
}
