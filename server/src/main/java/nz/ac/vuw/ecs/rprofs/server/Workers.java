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
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.data.RequestManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.RequestId;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 28/11/11
 */
@Singleton
public class Workers extends HttpServlet {

	private final Logger log = LoggerFactory.getLogger(Workers.class);

	private BlockingQueue<Continuation> workers = new ArrayBlockingQueue<Continuation>(40);

	private DatasetManager datasets;
	private RequestManager requests;

	@Inject
	Workers(DatasetManager datasets, RequestManager requests) {
		this.datasets = datasets;
		this.requests = requests;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final Continuation continuation = ContinuationSupport.getContinuation(req);
		byte[] data = (byte[]) req.getAttribute("Data");
		RequestId requestId = (RequestId) req.getAttribute("RequestId");
		String dataset = (String) req.getAttribute("Dataset");
		String records = (String) req.getAttribute("Records");
		Boolean flush = (Boolean) req.getAttribute("Flush");

		if (data != null) {
			// good to go
			resp.addHeader("Dataset", dataset);
			resp.addHeader("RequestId", String.valueOf(requestId.getValue()));
			resp.addHeader("Records", records);
			resp.setContentType("application/rprof");
			resp.setContentLength(data.length);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getOutputStream().write(data, 0, data.length);
			resp.getOutputStream().close();
			return;
		}

		if (flush != null && flush.booleanValue() == true) {
			workers.remove(continuation);
			if (requestId != null) {
				resp.addHeader("Dataset", dataset);
				resp.addHeader("RequestId", String.valueOf(requestId.getValue()));
				resp.addHeader("Flush", "true");
			}
			returnNoContent(resp);
			return;
		}

		if (continuation.isInitial()) {
			// first time through
			String hostname = req.getHeader("Hostname");
			boolean hasCache = req.getHeader("HasCache") != null;
			String request = req.getHeader("RequestId");
			dataset = req.getHeader("Dataset");
			req.setAttribute("Hostname", hostname);

			if (request != null) {
				Dataset ds = datasets.findDataset(dataset);
				Context.setDataset(ds);

				requestId = RequestId.create(request);
				// first time here
				if (hasCache) {
					// keep the request id
					req.setAttribute("RequestId", requestId);
					req.setAttribute("Dataset", dataset);
				} else {
					// no cache, so release the request
					requests.releaseRequest(requestId);
					requestId = null;
				}

				Context.clear();
			}
		}

		// check timeout -- flush as nothing else to do
		if (continuation.isExpired()) {
			workers.remove(continuation);
			if (requestId != null) {
				resp.addHeader("Dataset", dataset);
				resp.addHeader("RequestId", String.valueOf(requestId.getValue()));
				resp.addHeader("Flush", "true");
			}
			returnNoContent(resp);
			return;
		}

		if (requestId != null) {
			Dataset ds = datasets.findDataset(dataset);
			Context.setDataset(ds);

			// check still running
			if (ds.getStopped() != null) {
				workers.remove(continuation);
				resp.addHeader("Dataset", dataset);
				resp.addHeader("RequestId", String.valueOf(requestId.getValue()));
				resp.addHeader("Flush", "true");
				returnNoContent(resp);
				return;
			}

			Context.clear();
		}

		continuation.suspend();

		if (!workers.contains(continuation)) {
			workers.offer(continuation);
		}
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
