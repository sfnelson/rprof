package nz.ac.vuw.ecs.rprofs.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@SuppressWarnings("serial")
@Configurable(autowire=Autowire.BY_TYPE)
public class Logger extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);

	@Autowired private ContextManager contexts;

	@Autowired private DatasetService datasets;

	DBCollection events;

	public Logger() throws UnknownHostException, MongoException {
		Mongo mongo = new Mongo();
		DB db = mongo.getDB("rprof");
		events = db.getCollection("events");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Dataset current = datasets.findDataset(req.getHeader("Dataset"));
		ContextManager.setThreadLocal(current);

		ActiveContext active = contexts.getContext(current);

		parseEvents(active, req.getContentLength(), req.getInputStream());
		// List<Event> events = parseEvents(active, req.getContentLength(), req.getInputStream());
		// storeEvents(active, events);

		resp.setStatus(201);
		ContextManager.setThreadLocal(null);
	}

	protected List<Event> parseEvents(ActiveContext context, int length, InputStream in) throws IOException {
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

		List<Event> events = Collections.newList();

		for (int i = 0; i < length / RECORD_LENGTH; i++) {
			long thread = dis.readLong();
			int event = dis.readInt();
			short cnum = (short) dis.readInt();
			short mnum = (short) dis.readInt();
			int len = dis.readInt();

			if (len > MAX_PARAMETERS) {
				log.warn("warning: {} is greater than MAX_PARAMETERS\n", len);
				len = MAX_PARAMETERS;
			}

			long[] args = new long[Math.min(len, MAX_PARAMETERS)];
			for (int j = 0; j < MAX_PARAMETERS; j++) {
				long arg = dis.readLong();
				if (j < len) {
					args[j] = arg;
				}
			}

			//events.add(createEvent(context, thread, event, cnum, mnum, args));

			DBObject e = BasicDBObjectBuilder.start()
					.add("thread", thread)
					.add("event", event)
					.add("cnum", cnum)
					.add("mnum", mnum)
					.add("args", args).get();
			this.events.insert(e);
		}

		return events;
	}

	/*
	private Event createEvent(ActiveContext context, long threadId, int event, short cnum, short mnum, long[] args) {
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

		return new Event(ds, id, thread, event, type, attr, argList);
	}

	@Transactional
	private void storeEvents(ActiveContext context, List<Event> events) {
		for (Event event: events) {
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
	}

	@Transactional
	private Instance createInstance(Dataset ds, ObjectId id) {
		Instance i = new Instance(ds, id, null, null);
		em.persist(i);
		return i;
	}

	private Instance getInstance(Dataset ds, ObjectId id) {
		if (id == null) return null;

		Instance i = em.find(Instance.class, id);
		if (i == null) {
			i = createInstance(ds, id);
		}
		return i;
	} */
}
