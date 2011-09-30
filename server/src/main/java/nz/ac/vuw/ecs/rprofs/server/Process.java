package nz.ac.vuw.ecs.rprofs.server;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.reports.InstanceMapReduce;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 28/09/11
 */
@Configurable(autowire = Autowire.BY_TYPE)
public class Process extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Start.class);

	@VisibleForTesting
	@Autowired
	DatasetManager datasets;

	@VisibleForTesting
	@Autowired
	Context context;

	@Autowired
	Database db;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String handle = req.getParameter("dataset");
		String op = req.getParameter("op");

		Dataset dataset = datasets.findDataset(handle);
		context.setDataset(dataset);

		InstanceMapReduce mr = new InstanceMapReduce(dataset);

		Runnable process = db.createInstanceMapReduce(db.getEventQuery(), mr, true);
		new Thread(process).start();

		resp.addHeader("Dataset", dataset.getHandle());
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

	}

}