package nz.ac.vuw.ecs.rprofs.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.data.FieldManager;
import nz.ac.vuw.ecs.rprofs.server.data.InstanceManager;
import nz.ac.vuw.ecs.rprofs.server.data.MethodManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Attribute;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

@SuppressWarnings("serial")
public class Logger extends HttpServlet {

	private final ContextManager cm = ContextManager.getInstance();
	private final ClassManager classes = new ClassManager(cm);
	private final FieldManager fields = new FieldManager(cm);
	private final MethodManager methods = new MethodManager(cm);
	private final InstanceManager instances = new InstanceManager(cm);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {

		ActiveContext active = cm.getActive();
		cm.setCurrent(active.getDataset().getHandle());

		active.getContext().open();

		try {
			parse(active, req.getContentLength(), req.getInputStream());
		}
		finally {
			active.getContext().close();
		}

		resp.setStatus(201);
	}

	private void createEvent(ActiveContext context, long threadId, int event, short cnum, short mnum, long[] args) {
		Dataset ds = context.getDataset();

		EventId id = context.nextEvent();

		Instance thread = getInstance(ObjectId.create(ds, threadId));
		Class type = classes.find(ClassId.create(ds, cnum));

		Attribute<?> attr = null;
		if ((event & Event.FIELDS) != 0) {
			attr = fields.find(FieldId.create(ds, type, mnum));
		}
		else if ((event & Event.METHODS) != 0) {
			attr = methods.find(MethodId.create(ds, type, mnum));
		}

		ArrayList<Instance> argList = Collections.newList();
		for (long arg: args) {
			argList.add(getInstance(ObjectId.create(ds, arg)));
		}

		storeEvent(context, new Event(id, thread, event, type, attr, argList));
	}

	private void storeEvent(ActiveContext context, Event event) {
		Class cls = event.getType();
		Attribute<?> attr = event.getAttribute();
		Instance target = event.getFirstArg();

		switch (event.getEvent()) {
		case Event.METHOD_ENTER:
			if (attr != null && attr instanceof Method && ((Method) attr).isMain()) {
				context.setMainMethod(cls.getName());
			}
			break;
		case Event.METHOD_RETURN:
			if (attr != null && attr instanceof Method && ((Method) attr).isInit());
			else break;
		case Event.OBJECT_TAGGED:
			if (target != null) {
				target.setType(cls);
				target.setConstructor((Method) event.getAttribute());
			}
			break;
		}

		context.getContext().em().merge(event);
	}

	private Instance getInstance(ObjectId id) {
		if (id == null) return null;

		Instance i = instances.find(id);
		if (i == null) {
			i = instances.createInstance(id);
		}
		return i;
	}

	private void parse(ActiveContext context, int length, InputStream in) throws IOException {
		DataInputStream dis = new DataInputStream(in);

		//		#define MAX_PARAMETERS 16
		//		struct EventRecord {
		//			long int thread;
		//			char message[255];
		//			int cnum;
		//			int mnum;
		//			int len;
		//			long int params[MAX_PARAMETERS];
		//		}
		final int MAX_PARAMETERS = 16;
		final int RECORD_LENGTH = 8 + 4 + 4 + 4 + 4 + MAX_PARAMETERS * 8;

		for (int i = 0; i < length / RECORD_LENGTH; i++) {
			long thread = dis.readLong();
			int event = dis.readInt();
			short cnum = (short) dis.readInt();
			short mnum = (short) dis.readInt();
			int len = dis.readInt();

			if (len > MAX_PARAMETERS) {
				System.err.printf("warning: %d is greater than MAX_PARAMETERS\n", len);
				len = MAX_PARAMETERS;
			}

			long[] args = new long[Math.min(len, MAX_PARAMETERS)];
			for (int j = 0; j < MAX_PARAMETERS; j++) {
				long arg = dis.readLong();
				if (j < len) {
					args[j] = arg;
				}
			}

			createEvent(context, thread, event, cnum, mnum, args);
		}
	}
}
