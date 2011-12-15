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

		log.debug("receiving events");

		String dataset = req.getHeader("Dataset");

		byte[] buffer = new byte[req.getContentLength()];
		int read = 0;
		InputStream in = req.getInputStream();
		while (read < buffer.length) {
			int r = in.read(buffer, read, buffer.length - read);
			read += r;
		}
		log.debug("done reading: {} bytes", buffer.length);

		Continuation worker;
		try {
			while (true) {
				try {
					worker = workers.getWorker();
					while (true) {
						if (worker.isExpired()) {
							worker = workers.getWorker();
						} else break;
					}

					worker.setAttribute("dataset", dataset);
					worker.setAttribute("data", buffer);
					worker.resume();

					log.info("sent {} bytes to worker for {}", buffer.length, dataset);
					break;
				} catch (IllegalStateException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		} catch (InterruptedException ex) {
			throw new ServletException("interrupted while waiting for a worker", ex);
		}

		resp.setStatus(201);
		resp.setContentLength(0);
		resp.getOutputStream().close();
	}
}
