package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;
import nz.ac.vuw.ecs.rprofs.server.weaving.Weaver;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class Weave extends HttpServlet {

	private final java.util.logging.Logger log = java.util.logging.Logger.getLogger("logger");
	private final ContextManager cm = ContextManager.getInstance();

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		byte[] buffer, result;
		String cname;

		Map<String, String> headers = new HashMap<String, String>();
		for (Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			headers.put(key, req.getHeader(key));
		}
		int length = req.getContentLength();

		buffer = new byte[length];
		InputStream is = req.getInputStream();
		for (int i = 0; i < buffer.length;) {
			i += is.read(buffer, i, buffer.length - i);
		}

		ActiveContext active = cm.getActive();
		cm.setCurrent(active.getContext());
		active.getContext().open();

		try {
			Weaver weaver = new Weaver(active.nextClass());

			result = weaver.weave(buffer);

			cname = active.storeClass(weaver.getClassRecord()).getName();
		}
		finally {
			active.getContext().close();
		}

		resp.setStatus(200);
		resp.setContentLength(result.length);
		resp.setContentType("application/rprof");
		resp.getOutputStream().write(result);

		log.info(String.format("received %s (%d bytes), returning %d bytes", cname, length, result.length));

	}
}
