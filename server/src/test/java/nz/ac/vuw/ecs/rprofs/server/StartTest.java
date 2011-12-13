package nz.ac.vuw.ecs.rprofs.server;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/09/11
 */
public class StartTest {

	private Start start;
	private Dataset dataset;
	private DatasetManager manager;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@Before
	public void setup() {
		manager = createMock(DatasetManager.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);

		dataset = new Dataset(new DatasetId((short) 1), "foo", new Date(), "rprof_foo_1");
		start = new Start(manager);
	}

	@Test
	public void testDoGet() throws Exception {
		expect(request.getHeader(eq("Benchmark"))).andReturn("foo");
		expect(manager.createDataset("foo")).andReturn(dataset);
		response.addHeader("Dataset", "rprof_foo_1");
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);

		replay(request, response, manager);

		start.doGet(request, response);

		verify(request, response, manager);
	}
}
