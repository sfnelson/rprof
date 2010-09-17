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

import nz.ac.vuw.ecs.rprofs.server.data.LogRecord;

@SuppressWarnings("serial")
public class Logger extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {

		int length = req.getContentLength();

		List<LogRecord> records = parse(length, req.getInputStream());
		Context.getCurrent().storeLogs(records);
		
		resp.setStatus(201);
	}
	
	private static List<LogRecord> parse(int length, InputStream in) throws IOException {
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
		
		List<LogRecord> records = new ArrayList<LogRecord>();
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

			LogRecord record = LogRecord.create();
			record.thread = thread;
			record.event = event;
			record.cnum = cnum;
			record.mnum = mnum;
			record.args = args;
			records.add(record);
		}
		
		return records;
	}
}
