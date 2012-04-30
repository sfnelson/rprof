package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.reports.ClassMapReduce;
import nz.ac.vuw.ecs.rprofs.server.reports.FieldMapReduce;
import nz.ac.vuw.ecs.rprofs.server.reports.InstanceMapReduce;
import org.slf4j.LoggerFactory;

@Singleton
public class Stop extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Stop.class);

	private final DatasetManager datasets;
	private final Database db;

	@Inject
	Stop(DatasetManager datasets, Database db) {
		this.datasets = datasets;
		this.db = db;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String handle = req.getHeader("Dataset");

		Dataset dataset = datasets.findDataset(handle);
		datasets.stopDataset(dataset.getId());

		Dataset ds = datasets.findDataset(handle);

		log.info("profiler run stopped");

		Context.setDataset(dataset);
		final InstanceMapReduce mr = new InstanceMapReduce(dataset, db);
		db.createInstanceReducer(mr).reduce();
		Context.clear();

		final ClassMapReduce cmr = new ClassMapReduce(db.getClazzQuery());
		db.createClassSummaryMapper(db.getInstanceQuery(), cmr, true).map();
		db.createClassSummaryReducer(cmr).reduce();

		final FieldMapReduce fmr = new FieldMapReduce(db.getFieldQuery());
		db.createFieldSummaryMapper(db.getInstanceQuery(), fmr, true).map();
		db.createFieldSummaryReducer(fmr).reduce();

		log.info("finished map/reducing");

		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		resp.setContentLength(0);
	}

}
