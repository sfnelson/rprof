package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;

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
	private DatasetService datasets;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String dataset = req.getHeader("Dataset");
		datasets.stopDataset(dataset);
	}

}
