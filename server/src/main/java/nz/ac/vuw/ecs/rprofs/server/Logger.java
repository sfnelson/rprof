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

	@Autowired private DatasetService datasets;

	@Autowired private Mongo mongo;

	private DBCollection getEvents(Dataset ds) throws UnknownHostException, MongoException {
		DB db = mongo.getDB("rprof-" + ds.getHandle());
		return db.getCollection("events");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Dataset current = datasets.findDataset(req.getHeader("Dataset"));
		ContextManager.setThreadLocal(current);

		parseEvents(current, req.getContentLength(), req.getInputStream());

		resp.setStatus(201);
		ContextManager.setThreadLocal(null);
	}

	protected List<Event> parseEvents(Dataset current, int length, InputStream in) throws IOException {
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

		DBCollection ev = getEvents(current);

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

			DBObject e = BasicDBObjectBuilder.start()
					.add("thread", thread)
					.add("event", event)
					.add("cnum", cnum)
					.add("mnum", mnum)
					.add("args", args).get();
			ev.insert(e);
		}

		return events;
	}
}
