package nz.ac.vuw.ecs.rprofs.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

@SuppressWarnings("serial")
public class Logger extends HttpServlet {

	private final ContextManager cm = ContextManager.getInstance();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {

		int length = req.getContentLength();

		ActiveContext active = cm.getActive();
		active.getContext().open();

		try {
			List<Event> records = parse(active, length, req.getInputStream());
			active.storeLogs(records);
		}
		finally {
			active.getContext().close();
		}

		resp.setStatus(201);
	}

	private static List<Event> parse(ActiveContext context, int length, InputStream in)
	throws IOException {
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

		List<Event> records = new ArrayList<Event>();
		for (int i = 0; i < length / RECORD_LENGTH; i++) {
			long thread = dis.readLong();
			int event = dis.readInt();
			int cnum = dis.readInt();
			int mnum = dis.readInt();
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

			records.add(context.createEvent(thread, event, cnum, mnum, args));
		}

		return records;
	}
}
