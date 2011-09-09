package nz.ac.vuw.ecs.rprofs.server;

import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;
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
public class StartTest {

	private Start start;
	private Dataset dataset;
	private Database database;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@Before
	public void setup() {
		database = createMock(Database.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);

		dataset = new Dataset(new DataSetId((short) 1), "foo", new Date());
		start = new Start();
		start.database = database;
	}

	@Test
	public void testDoGet() throws Exception {
		expect(database.createDataset()).andReturn(dataset);
		response.addHeader("Dataset", "foo");
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);

		replay(request, response, database);

		start.doGet(request, response);

		verify(request, response, database);
	}
}
