package nz.ac.vuw.ecs.rprofs.server;

import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

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

	@Before
	public void setup() {
		manager = createMock(DatasetManager.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);

		dataset = new Dataset(new DatasetId((short) 1), "foo", new Date());
		stop = new Stop();
		stop.datasets = manager;
	}

	@Test
	public void testDoGet() throws Exception {

		expect(request.getHeader("Dataset")).andReturn("foo");
		expect(manager.findDataset("foo")).andReturn(dataset);
		manager.stopDataset(dataset);
		expect(manager.findDataset("foo")).andReturn(dataset);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);

		replay(request, response, manager);

		stop.doGet(request, response);

		verify(request, response, manager);
	}
}
