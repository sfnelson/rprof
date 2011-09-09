package nz.ac.vuw.ecs.rprofs.server;

import nz.ac.vuw.ecs.rprofs.server.data.EventManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.db.MongoEventBuilder;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import static nz.ac.vuw.ecs.rprofs.server.context.ContextManager.clearThreadLocal;
import static nz.ac.vuw.ecs.rprofs.server.context.ContextManager.setThreadLocal;

@SuppressWarnings("serial")
@Configurable(autowire = Autowire.BY_TYPE)
public class Logger extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);

	@Autowired
	private Database database;

	@Autowired
	private EventManager events;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Dataset current = database.getDataset(req.getHeader("Dataset"));
		setThreadLocal(current);

		parseEvents(current, req.getContentLength(), req.getInputStream());

		resp.setStatus(201);
		clearThreadLocal();
	}

	protected void parseEvents(Dataset ds, int length, InputStream in) throws IOException {
		DataInputStream dis = new DataInputStream(in);

		//		#define MAX_PARAMETERS 16
		//		struct EventRecord {
		//			long int id;
		//			long int thread;
		//			char message[255];
		//			int cnum;
		//			int mnum;
		//			int len;
		//			long int params[MAX_PARAMETERS];
		//		}
		final int MAX_PARAMETERS = 16;
		final int RECORD_LENGTH = 8 + 8 + 4 + 4 + 4 + 4 + MAX_PARAMETERS * 8;

		EventManager.EventBuilder builder = new MongoEventBuilder();
		builder.setDataSet(ds);

		for (int i = 0; i < length / RECORD_LENGTH; i++) {
			builder.setId(dis.readLong())
					.setThread(dis.readLong())
					.setEvent(dis.readInt())
					.setClazz((short) dis.readInt());

			if ((builder.getEvent() & Event.METHODS) == builder.getEvent()) {
				builder.setMethod((short) dis.readInt());
			} else {
				builder.setField((short) dis.readInt());
			}

			builder.clearArgs();
			int len = dis.readInt();

			if (len > MAX_PARAMETERS) {
				log.warn("warning: {} is greater than MAX_PARAMETERS\n", len);
				len = MAX_PARAMETERS;
			}

			for (int j = 0; j < MAX_PARAMETERS; j++) {
				long arg = dis.readLong();
				if (j < len) {
					builder.addArg(arg);
				}
			}

			database.storeEvent(ds, builder);
		}
	}
}
