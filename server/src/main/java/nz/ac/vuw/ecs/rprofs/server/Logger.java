package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.io.InputStream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.continuation.Continuation;
import org.slf4j.LoggerFactory;

@Singleton
public class Logger extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);

	private final Workers workers;

	@Inject
	Logger(Workers workers) {
		this.workers = workers;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		log.info("receiving events");

		Continuation worker;
		try {
			worker = workers.getWorker();
			while (worker.isExpired()) {
				worker = workers.getWorker();
				worker.getServletResponse();
			}
		} catch (InterruptedException ex) {
			throw new ServletException("interrupted while waiting for a worker", ex);
		}

		String dataset = req.getHeader("Dataset");
		worker.setAttribute("dataset", dataset);

		byte[] buffer = new byte[req.getContentLength()];
		int read = 0;
		InputStream in = req.getInputStream();
		while (read < buffer.length) {
			int r = in.read(buffer, read, buffer.length - read);
			read += r;
		}
		log.info("done reading");

		worker.setAttribute("data", buffer);
		worker.resume();

		resp.setStatus(201);
		resp.setContentLength(0);
		resp.getOutputStream().close();

		log.info("events sent to worker");
	}
}
