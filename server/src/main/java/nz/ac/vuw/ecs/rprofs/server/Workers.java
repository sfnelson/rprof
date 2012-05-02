package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 28/11/11
 */
@Singleton
public class Workers extends HttpServlet {

	private BlockingQueue<Continuation> workers = new ArrayBlockingQueue<Continuation>(40);

	private DatasetManager datasets;

	@Inject
	Workers(DatasetManager datasets) {
		this.datasets = datasets;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String dataset = (String) req.getAttribute("Dataset");
		byte[] data = (byte[]) req.getAttribute("Data");

		if (dataset == null || data == null) {
			final Continuation continuation = ContinuationSupport.getContinuation(req);

			if (req.getHeader("Dataset") != null) {
				// check still running
				Dataset ds = datasets.findDataset(req.getHeader("Dataset"));

				if (ds.getStopped() != null) {
					resp.addHeader("Flush", "true");
					returnNoContent(resp);
					workers.remove(continuation);
					return;
				}
			}

			if (continuation.isExpired()) {
				returnNoContent(resp);
				workers.remove(continuation);
				return;
			}

			if (!workers.contains(continuation)) {
				workers.offer(continuation);
			}

			continuation.suspend();
			return;
		}

		resp.addHeader("Dataset", dataset);
		resp.setContentType("application/rprof");
		resp.setContentLength(data.length);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getOutputStream().write(data, 0, data.length);
		resp.getOutputStream().close();
	}

	protected Continuation getWorker() throws InterruptedException {
		return workers.take();
	}

	protected void flush() {
		List<Continuation> toFlush = Lists.newArrayList();
		workers.drainTo(toFlush);
		for (Continuation c : toFlush) {
			c.resume();
		}
	}

	private void returnNoContent(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		resp.setContentLength(0);
		resp.getOutputStream().close();
	}
}
