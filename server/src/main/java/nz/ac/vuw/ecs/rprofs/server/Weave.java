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

import nz.ac.vuw.ecs.rprofs.server.data.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class Weave extends HttpServlet {

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {

		ActiveContext context = ContextManager.getInstance().getCurrent();

		Map<String, String> headers = new HashMap<String, String>();
		for (Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			headers.put(key, req.getHeader(key));
		}
		int length = req.getContentLength();

		byte[] buffer = new byte[length];
		InputStream is = req.getInputStream();
		for (int i = 0; i < buffer.length;) {
			i += is.read(buffer, i, buffer.length - i);
		}

		buffer = context.weaveClass(buffer);

		resp.setStatus(200);
		resp.setContentLength(buffer.length);
		resp.setContentType("application/rprof");
		resp.getOutputStream().write(buffer);
	}
}
