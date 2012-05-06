package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.Date;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.data.RequestManager;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetUpdater;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.ClassSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.reports.ClassMapReduce;
import nz.ac.vuw.ecs.rprofs.server.reports.FieldMapReduce;
import org.slf4j.LoggerFactory;

@Singleton
public class Stop extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Stop.class);

	private final DatasetManager datasets;
	private final Database db;
	private final Workers workers;
	private final RequestManager requests;

	@Inject
	Stop(DatasetManager datasets, Database db, Workers workers, RequestManager requests) {
		this.datasets = datasets;
		this.db = db;
		this.workers = workers;
		this.requests = requests;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String handle = req.getHeader("Dataset");
		String last = req.getHeader("Last-Event");

		Dataset dataset = datasets.findDataset(handle);
		datasets.stopDataset(dataset.getId());

		log.info("profiler run stopped");

		workers.flush(); // wake up all the workers in case their dataset has now finished

		Context.setDataset(dataset);

		try {
			requests.waitUntilDone();
		} catch (InterruptedException ex) {
			throw new ServletException(ex);
		}

		log.info("workers finished ({} instances)", db.getInstanceQuery().count());

		reduceClasses(dataset);
		reduceFields(dataset);

		log.info("finished map/reducing");

		DatasetUpdater<?> updater = db.getDatasetUpdater();
		updater.setFinished(new Date());
		if (last != null) {
			updater.setNumEvents(Long.valueOf(last));
		}
		updater.update(dataset.getId());

		Context.clear();

		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		resp.setContentLength(0);

		db.flush();
	}

	private void reduceClasses(Dataset dataset) {
		ClassMapReduce cmr = new ClassMapReduce(db.getClazzQuery());
		db.createMapReduce(Instance.class, ClassSummary.class, cmr).run();
	}

	private void reduceFields(Dataset dataset) {
		FieldMapReduce fmr = new FieldMapReduce(db.getFieldQuery());
		db.createMapReduce(Instance.class, FieldSummary.class, fmr).run();
	}
}
