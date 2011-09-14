package nz.ac.vuw.ecs.rprofs.server;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.MethodCreator;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.weaving.ConstructorWeavingTest;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
	private Method method;

	private DatasetManager manager;
	private ClassManager classes;
	private ClazzCreator builder;
	private MethodCreator mbuilder;
	private FieldCreator fbuilder;
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
		builder = createMock(ClazzCreator.class);
		mbuilder = createMock(MethodCreator.class);
		fbuilder = createMock(FieldCreator.class);

		dataset = new Dataset(new DatasetId((short) 1), "foo", new Date());
		clazz = new Clazz(ClazzId.create(dataset, 1), "org.foo.Bar", null, null, 0);
		method = new Method(MethodId.create(dataset, clazz, (short) 1),
				"<init>", clazz.getId(), clazz.getName(), "()V", Opcodes.ACC_PUBLIC);

		weave = new Weave();
		weave.classes = classes;
		weave.context = context;
		weave.datasets = manager;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDoPost() throws Exception {
		ServletInputStream data = new ServletInputStream();
		ServletOutputStream output = new ServletOutputStream();
		data.content = ConstructorWeavingTest.generateMinimalClass("org.foo.Bar");
		Capture<Integer> responseLength = new Capture<Integer>();

		// get properties
		expect(request.getHeader("Dataset")).andReturn("foobar");
		expect(manager.findDataset("foobar")).andReturn(dataset);
		context.setDataset(dataset);
		expect(request.getContentLength()).andReturn(data.content.length);
		expect(request.getInputStream()).andReturn(data);

		// init class
		expect(classes.createClazz()).andReturn(builder);
		expect(builder.setName("org.foo.Bar")).andReturn(builder);
		expect(builder.setParentName("java/lang/Object")).andReturn(builder);
		expect(builder.addMethod()).andReturn(mbuilder);
		expect(mbuilder.setAccess(Opcodes.ACC_PUBLIC)).andReturn(mbuilder);
		expect(mbuilder.setName("<init>")).andReturn(mbuilder);
		expect(mbuilder.setDescription("()V")).andReturn(mbuilder);
		expect(mbuilder.store()).andReturn(null);
		expect(builder.store()).andReturn(clazz.getId());

		expect(classes.getClazz(clazz.getId())).andReturn(clazz);
		expect(classes.findMethods(clazz.getId())).andReturn((List) Lists.newArrayList(method));
		expect(classes.findFields(clazz.getId())).andReturn((List) Lists.newArrayList());

		classes.setProperties(clazz.getId(), 0);

		// return
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentLength(EasyMock.capture(responseLength));
		response.setContentType("application/rprof");
		expect(response.getOutputStream()).andReturn(output);
		context.clear();

		replay(request, response, context, classes, manager, builder, mbuilder, fbuilder);

		weave.doPost(request, response);

		verify(request, response, context, classes, manager, builder, mbuilder, fbuilder);

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
