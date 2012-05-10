package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.io.InputStream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.data.RequestManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import org.eclipse.jetty.continuation.Continuation;
import org.slf4j.LoggerFactory;

@Singleton
public class Logger extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);

	private final DatasetManager datasets;
	private final Workers workers;
	private final RequestManager requests;

	@Inject
	Logger(DatasetManager datasets, Workers workers, RequestManager requests) {
		this.datasets = datasets;
		this.workers = workers;
		this.requests = requests;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		log.trace("receiving events");

		String dataset = req.getHeader("Dataset");
		String records = req.getHeader("Records");

		Dataset ds = datasets.findDataset(dataset);
		Context.setDataset(ds);

		byte[] buffer = new byte[req.getContentLength()];
		int read = 0;
		InputStream in = req.getInputStream();
		while (read < buffer.length) {
			int r = in.read(buffer, read, buffer.length - read);
			read += r;
		}
		log.trace("done reading: {} bytes", buffer.length);

		Continuation worker;
		try {
			while (true) {
				try {
					worker = workers.getWorker();
					while (true) {
						if (worker.isExpired()) {
							worker = workers.getWorker();
							continue;
						}
						if (worker.getAttribute("Dataset") == null) break;
						String wDataset = (String) worker.getAttribute("Dataset");
						if (dataset.equals(wDataset)) break;
						worker.setAttribute("Flush", true);
						worker.resume();
						continue;
					}

					String hostname = (String) worker.getAttribute("Hostname");
					if (worker.getAttribute("RequestId") == null) {
						worker.setAttribute("RequestId",
								requests.createRequest(
										hostname));
					}
					worker.setAttribute("Dataset", dataset);
					worker.setAttribute("Records", records);
					worker.setAttribute("Data", buffer);
					worker.resume();

					log.debug("sent event batch to {} ({})", hostname, dataset);
					break;
				} catch (IllegalStateException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		} catch (InterruptedException ex) {
			throw new ServletException("interrupted while waiting for a worker", ex);
		}

		Context.clear();

		resp.setStatus(201);
		resp.setContentLength(0);
		resp.getOutputStream().close();
	}
}
