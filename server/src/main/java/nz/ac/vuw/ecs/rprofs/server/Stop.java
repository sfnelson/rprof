package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.server.data.ContextManager;



/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class Stop extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		ContextManager cm = ContextManager.getInstance();
		cm.stop(cm.getCurrent().getDataset());
	}

}
