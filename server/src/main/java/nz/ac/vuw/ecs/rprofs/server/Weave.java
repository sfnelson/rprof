package nz.ac.vuw.ecs.rprofs.server;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;
import nz.ac.vuw.ecs.rprofs.server.weaving.Weaver;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static nz.ac.vuw.ecs.rprofs.server.context.ContextManager.clearThreadLocal;
import static nz.ac.vuw.ecs.rprofs.server.context.ContextManager.setThreadLocal;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
@Configurable(autowire = Autowire.BY_TYPE)
public class Weave extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Weave.class);

	@Autowired
	private ContextManager cm;

	@Autowired
	private Database database;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		byte[] buffer, result;
		String cname;

		Dataset ds = database.getDataset(req.getHeader("Dataset"));
		setThreadLocal(ds);

		int length = req.getContentLength();

		log.debug("received class weave request ({}, {})", ds, length);

		buffer = new byte[length];
		InputStream is = req.getInputStream();
		for (int i = 0; i < buffer.length; ) {
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

		log.info(cname);
		log.debug("returning {} bytes", new Object[]{result.length});

		clearThreadLocal();
	}
}
