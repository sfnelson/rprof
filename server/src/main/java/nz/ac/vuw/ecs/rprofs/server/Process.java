package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Result;
import nz.ac.vuw.ecs.rprofs.server.reports.ResultMapReduce;
import org.slf4j.LoggerFactory;

@Singleton
public class Process extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Start.class);

	private final DatasetManager datasets;
	private final Database db;

	@Inject
	Process(DatasetManager datasets, Database db) {
		this.datasets = datasets;
		this.db = db;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		final String handle = req.getParameter("dataset");
		final String op = req.getParameter("op");

		final Dataset dataset = datasets.findDataset(handle);
		if (dataset != null) {
			Context.setDataset(dataset);

			final ResultMapReduce mr = new ResultMapReduce(db.getClazzQuery());

			if (op == null || op.equals("map")) {
				db.createResultMapper(db.getInstanceQuery(), mr, true).map();
			}
			if (op == null || op.equals("reduce")) {
				db.createResultReducer(mr).reduce();
			}
			if (op == null || op.equals("print")) {
				print(resp);
				return;
			}

			Context.clear();
		}

		resp.addHeader("Dataset", dataset.getDatasetHandle());
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		resp.setContentLength(0);

		resp.getOutputStream().close();
	}

	private void print(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		ServletOutputStream out = resp.getOutputStream();

		Query.Cursor<? extends Result> query = db.getResultQuery().find();
		while (query.hasNext()) {
			Result r = query.next();
			out.print(r.getClassName());
			for (int i : r.getTotals()) {
				out.print(",");
				out.print(i);
			}
			for (int i : r.getCounts()) {
				out.print(",");
				out.print(i);
			}
			out.println();
		}

		resp.getOutputStream().close();
	}
}