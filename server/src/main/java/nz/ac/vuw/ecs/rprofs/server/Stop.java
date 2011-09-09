package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
@Configurable(autowire=Autowire.BY_TYPE)
public class Stop extends HttpServlet {

	@Autowired
	private ContextManager contexts;

	@Autowired
	private Database database;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Dataset dataset = database.getDataset(req.getHeader("Dataset"));
		contexts.stopRecording(dataset);

		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

}
