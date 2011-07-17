package nz.ac.vuw.ecs.rprofs.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.model.Attribute;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("serial")
@Configurable(autowire=Autowire.BY_TYPE)
public class Logger extends HttpServlet {

	private final java.util.logging.Logger log = java.util.logging.Logger.getLogger("event-logger");

	@PersistenceContext
	private EntityManager em;

	@Autowired private ContextManager contexts;

	@Autowired private DatasetService datasets;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Dataset current = datasets.findDataset(req.getHeader("Dataset"));
		ContextManager.setThreadLocal(current);

		ActiveContext active = contexts.getContext(current);

		parse(active, req.getContentLength(), req.getInputStream());

		resp.setStatus(201);
		ContextManager.setThreadLocal(null);
	}

	private void createEvent(ActiveContext context, long threadId, int event, short cnum, short mnum, long[] args) {
		Dataset ds = ContextManager.getThreadLocal();

		EventId id = context.nextEvent();

		Instance thread = getInstance(ds, ObjectId.create(ds, threadId));
		Class type = em.find(Class.class, (ClassId.create(ds, cnum)));

		// check consistency
		if (ds == null) {
			log.warning("dataset is null");
		}

		Attribute<?> attr = null;
		if ((event & (Event.FIELDS | Event.METHODS)) != 0) {
			if (type == null) {
				log.warning(String.format("type not found: %d", cnum));
			}
			else if ((event & Event.FIELDS) != 0) {
				attr = em.find(Field.class, FieldId.create(ds, type, mnum));
				if (attr == null && type != null) {
					log.warning(String.format("field not found: %s.%d (%d)", type.getName(), mnum, event));
				}
			}
			else if ((event & Event.METHODS) != 0) {
				attr = em.find(Method.class, MethodId.create(ds, type, mnum));
				if (attr == null) {
					log.warning(String.format("method not found: %s.%d (%d)", type.getName(), mnum, event));
				}
			}
		}

		ArrayList<Instance> argList = Collections.newList();
		for (long arg: args) {
			argList.add(getInstance(ds, ObjectId.create(ds, arg)));
		}

		storeEvent(context, new Event(ds, id, thread, event, type, attr, argList));
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

		em.merge(event);
	}

	private Instance getInstance(Dataset ds, ObjectId id) {
		if (id == null) return null;

		Instance i = em.find(Instance.class, id);
		if (i == null) {
			i = new Instance(ds, id, null, null);
			em.persist(i);
		}
		return i;
	}

	@Transactional
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
				log.warning(String.format("warning: %d is greater than MAX_PARAMETERS\n", len));
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
