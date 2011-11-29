package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.reports.InstanceMapReduce;
import nz.ac.vuw.ecs.rprofs.server.reports.MapReduceTask;
import org.slf4j.LoggerFactory;

@Singleton
public class Process extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Start.class);

	private final DatasetManager datasets;
	private final Database db;
	private final ClassManager classes;

	@Inject
	Process(DatasetManager datasets, Database db, ClassManager classes) {
		this.datasets = datasets;
		this.db = db;
		this.classes = classes;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		final String handle = req.getParameter("dataset");
		final String op = req.getParameter("op");

		final Dataset dataset = datasets.findDataset(handle);
		Context.setDataset(dataset);

		InstanceMapReduce mr = new InstanceMapReduce(dataset, db);

		final MapReduceTask<Event> task = db.createInstanceMapReduce(db.getEventQuery(), mr, true);
		new Thread() {
			@Override
			public void run() {
				Context.setDataset(dataset);
				if (op == null) task.run();
				else if (op.equals("map")) {
					task.map();
				} else if (op.equals("reduce")) {
					task.reduce();
				}
				Context.clear();
			}
		}.start();

		Context.clear();

		resp.addHeader("Dataset", dataset.getHandle());
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		resp.setContentLength(0);

		resp.getOutputStream().close();
	}

}