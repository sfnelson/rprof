package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.mongodb.Mongo;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;
import org.junit.*;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/09/11
 */
public class DatabaseTest {

	private static Process process;

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
		pb.command(path,  "--dbpath", "/tmp/rprof/mongo",
				"--bind_ip", "127.0.0.1", "--port", "27018");
		process = pb.start();
	}

	private Database database;
	private Mongo mongo;

	@Before
	public void createMongo() throws Exception {
		mongo = new Mongo("127.0.0.1", 27018);
		database = new Database(mongo);
	}

	@Test
	public void testCreateDataset() throws Exception {
		assertEquals(Lists.newArrayList("admin"), mongo.getDatabaseNames());

		Dataset dataset = database.createDataset();
		assertNotNull(dataset);
		assertNotNull(dataset.getId());
		assertTrue(dataset.getId().indexValue() > 0);
		assertNotNull(dataset.getHandle());
		assertFalse(dataset.getHandle().isEmpty());
		assertNotNull(dataset.getRpcId());
		assertEquals(dataset.getId().indexValue(), dataset.getRpcId().shortValue());

		assertEquals(2, mongo.getDatabaseNames().size());

		List<String> dbs = mongo.getDatabaseNames();
		dbs.remove("test");
		dbs.remove("admin");
		assertEquals(1, dbs.size());

		String dbname = dbs.get(0);
		assertEquals(dbname, database.getName(dataset));
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals("rprof_foo_0", database.getName(new Dataset(new DataSetId((short) 0), "foo", new Date())));
		assertEquals("rprof_20110101_1", database.getName(new Dataset(new DataSetId((short) 1), "20110101", new Date())));
	}

	@Test
	public void testGetNextId() throws Exception {
		assertEquals((short) 1, database.getNextId());
		database.createDataset();
		assertEquals((short) 2, database.getNextId());
		database.createDataset();
		assertEquals((short) 3, database.getNextId());
	}

	@Test
	public void testGetDatasets() throws Exception {
		assertEquals(new ArrayList<Dataset>(), database.getDatasets());
		Dataset ds1 = database.createDataset();
		assertEquals(Lists.newArrayList(ds1), database.getDatasets());
		Dataset ds2 = database.createDataset();

		List<Dataset> result = database.getDatasets();
		Collections.sort(result);
		assertEquals(Lists.newArrayList(ds1, ds2), result);
	}

	@Test
	public void testGetDataSetDatasetId() throws Exception {
		Dataset in = database.createDataset();
		assertEquals(in, database.getDataSet(in.getId()));

		assertNull(database.getDataSet(new DataSetId((short) 0)));
	}

	@Test
	public void testGetDataSetLong() throws Exception {
		Dataset in = database.createDataset();
		assertEquals(in, database.getDataSet(in.getId().longValue()));

		assertNull(database.getDataSet(0l));
	}

	@Test
	public void testGetDataSetHandle() throws Exception {
		Dataset in = database.createDataset();
		assertEquals(in, database.getDataset(in.getHandle()));

		assertNull(database.getDataset("foobar"));
	}

	@Test
	public void testDropDataset() throws Exception {
		Dataset in = database.createDataset();
		assertEquals(Lists.newArrayList(in), database.getDatasets());
		database.dropDataset(in);
		assertEquals(new ArrayList<Dataset>(), database.getDatasets());

		assertNull(database.dropDataset(in));
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

		for (Scanner sc = new Scanner(process.getInputStream()); sc.hasNextLine();) {
			System.out.println(sc.nextLine());
		}
		for (Scanner sc = new Scanner(process.getErrorStream()); sc.hasNextLine();) {
			System.err.println(sc.nextLine());
		}

		ProcessBuilder pb = new ProcessBuilder();
		pb.command("/bin/rm", "-rf", "/tmp/rprof");
		pb.start();
	}
}
