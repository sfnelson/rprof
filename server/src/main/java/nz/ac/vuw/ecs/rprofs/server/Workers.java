package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 28/11/11
 */
@Singleton
public class Workers extends HttpServlet {

	private BlockingQueue<Continuation> workers = new ArrayBlockingQueue<Continuation>(40);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String dataset = (String) req.getAttribute("dataset");
		byte[] data = (byte[]) req.getAttribute("data");

		if (dataset == null || data == null) {
			final Continuation continuation = ContinuationSupport.getContinuation(req);

			if (continuation.isExpired()) {
				resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
				resp.setContentLength(0);
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
	}

	protected Continuation getWorker() throws InterruptedException {
		return workers.take();
	}
}
