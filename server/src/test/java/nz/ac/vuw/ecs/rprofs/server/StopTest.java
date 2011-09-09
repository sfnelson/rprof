package nz.ac.vuw.ecs.rprofs.server;

import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;
import org.easymock.EasyMock;
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
	private Database database;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@Before
	public void setup() {
		database = createMock(Database.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);

		dataset = new Dataset(new DataSetId((short) 1), "foo", new Date());
		stop = new Stop();
		stop.database = database;
	}

	@Test
	public void testDoGet() throws Exception {

		expect(request.getHeader("Dataset")).andReturn("foo");
		expect(database.getDataset(EasyMock.eq("foo"))).andReturn(dataset);
		expect(database.setStopped(EasyMock.same(dataset), EasyMock.anyObject(Date.class)))
				.andReturn(dataset);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);

		replay(request, response, database);

		stop.doGet(request, response);

		verify(request, response, database);
	}
}
