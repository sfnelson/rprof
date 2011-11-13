package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.data.EventManager;
import nz.ac.vuw.ecs.rprofs.server.data.util.EventCreator;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import org.junit.Before;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public class LoggerTest {

	Dataset ds;

	Logger logger;

	Database database;
	DatasetManager datasets;
	Context context;
	EventManager events;
	HttpServletRequest request;
	HttpServletResponse response;
	EventCreator builder;

	// TODO tests disabled while I'm working on incremental map/reduce

	@Before
	public void setup() {
		ds = new Dataset(new DatasetId((short) 1), "foobar", new Date());

		database = createMock(Database.class);
		datasets = createMock(DatasetManager.class);
		context = createMock(Context.class);
		events = createMock(EventManager.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);
		builder = createMock(EventCreator.class);

		logger = new Logger(datasets, events, context, database, null);
	}

	//@Test
	public void testDoPost() throws Exception {

		expect(request.getHeader("Dataset")).andReturn("foobar");
		expect(datasets.findDataset("foobar")).andReturn(ds);
		context.setDataset(ds);

		expect(request.getContentLength()).andReturn(0);
		expect(request.getInputStream()).andReturn(null);
		expect(events.createEvent()).andReturn(null);
		database.flush();
		response.setStatus(HttpServletResponse.SC_CREATED);
		context.clear();

		replay(datasets, context, events, request, response, database);

		logger.doPost(request, response);

		verify(datasets, context, events, request, response, database);
	}

	//@Test
	public void testParseNoEvents() throws Exception {
		ServletInputStream in = new ServletInputStream();
		in.content = new byte[]{};

		expect(events.createEvent()).andReturn(builder);
		database.flush();

		replay(datasets, context, events, builder, database);

		logger.parseEvents(ds, 0, in);

		verify(datasets, context, events, builder, database);
	}

	//@Test
	public void testParseMethodEvent() throws Exception {
		ServletInputStream in = new ServletInputStream();

		// 8 + 8 + 4 + 4 + 4 + 4 + MAX_PARAMETERS * 8
		in.content = new byte[]{
				0, 0, 0, 0, 0, 0, 0, 1,
				0, 0, 0, 0, 0, 0, 0, 2,
				0, 0, 0, Event.METHOD_ENTER,
				0, 0, 0, 3,
				0, 0, 0, 5,
				0, 0, 0, 6,
				0, 0, 0, 0, 0, 0, 0, 7,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 9,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 12,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0
		};

		EventId id = EventId.create(ds, 1);
		InstanceId thread = InstanceId.create(ds, 2);
		int event = 4;
		ClazzId clazz = ClazzId.create(ds, 3);
		MethodId method = MethodId.create(ds, clazz, (short) 5);
		List<InstanceId> args = Lists.newArrayList(
				InstanceId.create(ds, 7), null,
				InstanceId.create(ds, 9), null, null,
				InstanceId.create(ds, 12)
		);

		expect(events.createEvent()).andReturn(builder);
		expect(builder.setId(eq(id))).andReturn(builder);
		expect(builder.setThread(thread)).andReturn(builder);
		expect(builder.setEvent(event)).andReturn(builder);
		expect(builder.setClazz(clazz)).andReturn(builder);
		expect(builder.setMethod(method)).andReturn(builder);
		for (InstanceId arg : args) {
			expect(builder.addArg(arg)).andReturn(builder);
		}
		expect(builder.store()).andReturn(null);
		database.flush();

		replay(datasets, context, events, builder, database);

		logger.parseEvents(ds, in.content.length, in);

		verify(datasets, context, events, builder, database);
		assertEquals(in.content.length, in.count);
	}

	//@Test
	public void testParseFieldEvent() throws Exception {
		ServletInputStream in = new ServletInputStream();

		// 8 + 8 + 4 + 4 + 4 + 4 + MAX_PARAMETERS * 8
		in.content = new byte[]{
				0, 0, 0, 0, 0, 0, 0, 2,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, Event.FIELD_WRITE,
				0, 0, 0, 1,
				0, 0, 0, 2,
				0, 0, 0, 2,
				0, 0, 0, 0, 0, 0, 0, 1,
				0, 0, 0, 0, 0, 0, 0, 2,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0
		};

		EventId id = EventId.create(ds, 2);
		InstanceId thread = null;
		int event = Event.FIELD_WRITE;
		ClazzId clazz = ClazzId.create(ds, 1);
		FieldId field = FieldId.create(ds, clazz, (short) 2);
		List<InstanceId> args = Lists.newArrayList(
				InstanceId.create(ds, 1),
				InstanceId.create(ds, 2)
		);

		expect(events.createEvent()).andReturn(builder);
		expect(builder.setId(eq(id))).andReturn(builder);
		expect(builder.setThread(thread)).andReturn(builder);
		expect(builder.setEvent(event)).andReturn(builder);
		expect(builder.setClazz(clazz)).andReturn(builder);
		expect(builder.setField(field)).andReturn(builder);
		for (InstanceId arg : args) {
			expect(builder.addArg(arg)).andReturn(builder);
		}
		expect(builder.store()).andReturn(null);
		database.flush();

		replay(datasets, context, events, builder, database);

		logger.parseEvents(ds, in.content.length, in);

		verify(datasets, context, events, builder, database);
		assertEquals(in.content.length, in.count);
	}

	//@Test
	public void testParseBadEvent() throws Exception {
		ServletInputStream in = new ServletInputStream();

		// 8 + 8 + 4 + 4 + 4 + 4 + MAX_PARAMETERS * 8
		in.content = new byte[]{
				0, 0, 0, 0, 0, 0, 0, 3,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, Event.OBJECT_ALLOCATED,
				0, 0, 0, 0,
				0, 0, 0, 0,
				0, 0, 0, 17,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,

				0, 0, 0, 0, 0, 0, 0, 3,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, Event.CLASS_WEAVE,
				0, 0, 0, 3,
				0, 0, 0, 0,
				0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0
		};

		EventId id = EventId.create(ds, 3);
		InstanceId thread = null;
		int event1 = Event.OBJECT_ALLOCATED;
		int event2 = Event.CLASS_WEAVE;
		ClazzId clazz = ClazzId.create(ds, 3);
		List<InstanceId> args = Lists.newArrayList(
				null, null, null, null,
				null, null, null, null,
				null, null, null, null,
				null, null, null, null
		);

		expect(events.createEvent()).andReturn(builder);
		expect(builder.setId(eq(id))).andReturn(builder);
		expect(builder.setThread(thread)).andReturn(builder);
		expect(builder.setEvent(event1)).andReturn(builder);
		for (InstanceId arg : args) {
			expect(builder.addArg(arg)).andReturn(builder);
		}
		expect(builder.store()).andReturn(null);

		expect(builder.setId(eq(id))).andReturn(builder);
		expect(builder.setThread(thread)).andReturn(builder);
		expect(builder.setEvent(event2)).andReturn(builder);
		expect(builder.setClazz(clazz)).andReturn(builder);
		expect(builder.store()).andReturn(null);
		database.flush();

		replay(datasets, context, events, builder, database);

		logger.parseEvents(ds, in.content.length, in);

		verify(datasets, context, events, builder, database);
		assertEquals(in.content.length, in.count);
	}

	private class ServletInputStream extends javax.servlet.ServletInputStream {
		int count = 0;
		byte[] content;

		public int read() throws IOException {
			if (count < content.length)
				return (int) content[count++];
			else throw new IOException("read past the end of the file: " + count);
		}
	}
}
