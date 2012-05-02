package nz.ac.vuw.ecs.rprofs.server;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import nz.ac.vuw.ecs.rprofs.server.reports.MapReduce;
import nz.ac.vuw.ecs.rprofs.server.reports.Reducer;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/09/11
 */
public class StopTest {

	private Stop stop;
	private Dataset dataset;
	private DatasetManager manager;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Database database;
	private Reducer.ReducerTask reduce;
	private Workers workers;

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		manager = createMock(DatasetManager.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);
		database = createMock(Database.class);
		reduce = createMock(Reducer.ReducerTask.class);
		workers = createMock(Workers.class);

		dataset = new Dataset(new DatasetId((short) 1), "foo", new Date(), "rprof_foo_1");
		stop = new Stop(manager, database, workers);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDoGet() throws Exception {

		expect(request.getHeader("Dataset")).andReturn("rprof_foo_1");
		expect(manager.findDataset("rprof_foo_1")).andReturn(dataset);
		manager.stopDataset(dataset.getId());
		expect(manager.findDataset("rprof_foo_1")).andReturn(dataset);
		workers.flush();

		expect(database.createInstanceReducer(EasyMock.<MapReduce>anyObject())).andReturn(reduce);
		reduce.reduce();

		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		response.setContentLength(0);

		replay(request, response, manager, database, reduce, workers);

		stop.doGet(request, response);

		verify(request, response, manager, database, reduce, workers);
	}
}
