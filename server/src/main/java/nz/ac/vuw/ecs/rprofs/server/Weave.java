package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;
import nz.ac.vuw.ecs.rprofs.server.weaving.Weaver;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
@Configurable(autowire=Autowire.BY_TYPE)
public class Weave extends HttpServlet {

	private final java.util.logging.Logger log = java.util.logging.Logger.getLogger("logger");

	@Autowired
	private ContextManager cm;

	@Autowired
	private DatasetService datasets;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		byte[] buffer, result;
		String cname;

		Dataset ds = datasets.findDataset(req.getHeader("Dataset"));
		ContextManager.setThreadLocal(ds);

		int length = req.getContentLength();

		log.fine(String.format("received class weave request (%s, %d)", ds, length));

		buffer = new byte[length];
		InputStream is = req.getInputStream();
		for (int i = 0; i < buffer.length;) {
			i += is.read(buffer, i, buffer.length - i);
		}

		ActiveContext active = cm.getContext(ds);

		Weaver weaver = new Weaver(active.nextClass());
		result = weaver.weave(buffer);
		cname = active.storeClass(weaver.getClassRecord()).getName();

		resp.setStatus(200);
		resp.setContentLength(result.length);
		resp.setContentType("application/rprof");
		resp.getOutputStream().write(result);

		log.finest(String.format("received %s (%d bytes), returning %d bytes", cname, length, result.length));
		ContextManager.setThreadLocal(null);
	}
}
