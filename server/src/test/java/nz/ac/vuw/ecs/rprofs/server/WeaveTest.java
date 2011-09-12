package nz.ac.vuw.ecs.rprofs.server;

import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import nz.ac.vuw.ecs.rprofs.server.weaving.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.weaving.ConstructorWeavingTest;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/09/11
 */
public class WeaveTest {

	private Weave weave;
	private Dataset dataset;
	private Clazz clazz;

	private DatasetManager manager;
	private ClassManager classes;
	private Context context;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@Before
	public void setup() {
		classes = createMock(ClassManager.class);
		manager = createMock(DatasetManager.class);
		context = createMock(Context.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);

		dataset = new Dataset(new DatasetId((short) 1), "foo", new Date());
		clazz = new Clazz(ClazzId.create(dataset, 1), "org.foo.Bar", null, null, 0);

		weave = new Weave();
		weave.classes = classes;
		weave.context = context;
		weave.datasets = manager;
	}

	@Test
	public void testDoPost() throws Exception {
		ServletInputStream data = new ServletInputStream();
		ServletOutputStream output = new ServletOutputStream();
		data.content = ConstructorWeavingTest.generateMinimalClass("org.foo.Bar");
		Capture<Integer> responseLength = new Capture<Integer>();

		expect(request.getHeader("Dataset")).andReturn("foobar");
		expect(manager.findDataset("foobar")).andReturn(dataset);
		context.setDataset(dataset);
		expect(request.getContentLength()).andReturn(data.content.length);
		expect(request.getInputStream()).andReturn(data);
		expect(classes.createClass()).andReturn(clazz.getId());
		expect(classes.storeClass(EasyMock.anyObject(ClassRecord.class))).andReturn(clazz);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentLength(EasyMock.capture(responseLength));
		response.setContentType("application/rprof");
		expect(response.getOutputStream()).andReturn(output);
		context.clear();

		replay(request, response, context, classes, manager);

		weave.doPost(request, response);

		verify(request, response, context, classes, manager);

		assertEquals(data.content.length, data.count); // ensure whole class was read.
		assertEquals(responseLength.getValue().intValue(), output.count); // ensure whole class was written
	}

	private class ServletInputStream extends javax.servlet.ServletInputStream {
		int count = 0;
		byte[] content;

		public int read() throws IOException {
			if (count < content.length)
				return (int) content[count++];
			else throw new RuntimeException("read past the end of the file");
		}
	}

	private class ServletOutputStream extends javax.servlet.ServletOutputStream {
		int count = 0;

		public void write(int value) {
			count++;
		}
	}
}
