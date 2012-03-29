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

import static nz.ac.vuw.ecs.rprofs.server.domain.Result.*;

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

			if (op == null || op.equals("map") || op.equals("mapreduce")) {
				db.createResultMapper(db.getInstanceQuery(), mr, true).map();
			}
			if (op == null || op.equals("reduce") || op.equals("mapreduce")) {
				db.createResultReducer(mr).reduce();
			}
			if (op == null || op.equals("print")) {
				print(resp);
				return;
			}
			if (op.equals("summary")) {
				summary(resp);
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
			for (int i : r.getNone()) {
				out.print(",");
				out.print(i);
			}
			for (int i : r.getCol()) {
				out.print(",");
				out.print(i);
			}
			for (int i : r.getEq()) {
				out.print(",");
				out.print(i);
			}
			for (int i : r.getEqCol()) {
				out.print(",");
				out.print(i);
			}
			out.println();
		}

		resp.getOutputStream().close();
	}

	private void summary(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		ServletOutputStream out = resp.getOutputStream();

		int[] eqcol = new int[7];
		int[] eq = new int[7];
		int[] col = new int[7];
		int[] none = new int[7];

		Query.Cursor<? extends Result> query = db.getResultQuery().find();
		while (query.hasNext()) {
			Result r = query.next();

			getTotals(eqcol, r.getEqCol());
			getTotals(eq, r.getEq());
			getTotals(col, r.getCol());
			getTotals(none, r.getNone());
		}

		out.println("Set,Constructor,Coarse,Fine,Equals,Collection,Immutable,Total");

		out.print("All Objects");
		for (int i = 0; i < none.length; i++) {
			out.print(",");
			out.print(eqcol[i] + eq[i] + col[i] + none[i]);
		}
		out.println();

		out.print("In Equals");
		for (int i = 0; i < none.length; i++) {
			out.print(",");
			out.print(eqcol[i] + eq[i]);
		}
		out.println();

		out.print("In Collection");
		for (int i = 0; i < none.length; i++) {
			out.print(",");
			out.print(eqcol[i] + col[i]);
		}
		out.println();

		out.print("In Both");
		for (int i = 0; i < none.length; i++) {
			out.print(",");
			out.print(eqcol[i]);
		}
		out.println();

		out.print("In Neither");
		for (int i = 0; i < none.length; i++) {
			out.print(",");
			out.print(none[i]);
		}
		out.println();


		resp.getOutputStream().close();
	}

	private void getTotals(int[] totals, int[] input) {
		int constructor = 0;
		int fine = 0;
		int coarse = 0;
		int equals = 0;
		int collection = 0;
		int immutable = 0;
		int total = 0;

		immutable += input[CONSTRUCTOR_COARSE_FINE_EQUALS_COLL];
		immutable += input[CONSTRUCTOR_COARSE_FINE_EQUALS];
		immutable += input[CONSTRUCTOR_COARSE_FINE_COLL];
		immutable += input[CONSTRUCTOR_COARSE_FINE];
		immutable += input[CONSTRUCTOR_COARSE_EQUALS_COLL];
		immutable += input[CONSTRUCTOR_COARSE_EQUALS];
		immutable += input[CONSTRUCTOR_COARSE_COLL];
		immutable += input[CONSTRUCTOR_COARSE];
		immutable += input[CONSTRUCTOR_FINE_EQUALS_COLL];
		immutable += input[CONSTRUCTOR_FINE_EQUALS];
		immutable += input[CONSTRUCTOR_FINE_COLL];
		immutable += input[CONSTRUCTOR_FINE];
		immutable += input[CONSTRUCTOR_EQUALS_COLL];
		immutable += input[CONSTRUCTOR_EQUALS];
		immutable += input[CONSTRUCTOR_COLL];
		immutable += input[CONSTRUCTOR];
		immutable += input[COARSE_FINE_EQUALS_COLL];
		immutable += input[COARSE_FINE_EQUALS];
		immutable += input[COARSE_FINE_COLL];
		immutable += input[COARSE_FINE];
		immutable += input[COARSE_EQUALS_COLL];
		immutable += input[COARSE_EQUALS];
		immutable += input[COARSE_COLL];
		immutable += input[COARSE];
		immutable += input[FINE_EQUALS_COLL];
		immutable += input[FINE_EQUALS];
		immutable += input[FINE_COLL];
		immutable += input[FINE];
		immutable += input[EQUALS_COLL];
		immutable += input[EQUALS];
		immutable += input[COLL];

		constructor += input[CONSTRUCTOR_COARSE_FINE_EQUALS_COLL];
		constructor += input[CONSTRUCTOR_COARSE_FINE_EQUALS];
		constructor += input[CONSTRUCTOR_COARSE_FINE_COLL];
		constructor += input[CONSTRUCTOR_COARSE_FINE];
		constructor += input[CONSTRUCTOR_COARSE_EQUALS_COLL];
		constructor += input[CONSTRUCTOR_COARSE_EQUALS];
		constructor += input[CONSTRUCTOR_COARSE_COLL];
		constructor += input[CONSTRUCTOR_COARSE];
		constructor += input[CONSTRUCTOR_FINE_EQUALS_COLL];
		constructor += input[CONSTRUCTOR_FINE_EQUALS];
		constructor += input[CONSTRUCTOR_FINE_COLL];
		constructor += input[CONSTRUCTOR_FINE];
		constructor += input[CONSTRUCTOR_EQUALS_COLL];
		constructor += input[CONSTRUCTOR_EQUALS];
		constructor += input[CONSTRUCTOR_COLL];
		constructor += input[CONSTRUCTOR];

		coarse += input[CONSTRUCTOR_COARSE_FINE_EQUALS_COLL];
		coarse += input[CONSTRUCTOR_COARSE_FINE_EQUALS];
		coarse += input[CONSTRUCTOR_COARSE_FINE_COLL];
		coarse += input[CONSTRUCTOR_COARSE_FINE];
		coarse += input[CONSTRUCTOR_COARSE_EQUALS_COLL];
		coarse += input[CONSTRUCTOR_COARSE_EQUALS];
		coarse += input[CONSTRUCTOR_COARSE_COLL];
		coarse += input[CONSTRUCTOR_COARSE];
		coarse += input[COARSE_FINE_EQUALS_COLL];
		coarse += input[COARSE_FINE_EQUALS];
		coarse += input[COARSE_FINE_COLL];
		coarse += input[COARSE_FINE];
		coarse += input[COARSE_EQUALS_COLL];
		coarse += input[COARSE_EQUALS];
		coarse += input[COARSE_COLL];
		coarse += input[COARSE];

		fine += input[CONSTRUCTOR_COARSE_FINE_EQUALS_COLL];
		fine += input[CONSTRUCTOR_COARSE_FINE_EQUALS];
		fine += input[CONSTRUCTOR_COARSE_FINE_COLL];
		fine += input[CONSTRUCTOR_COARSE_FINE];
		fine += input[CONSTRUCTOR_FINE_EQUALS_COLL];
		fine += input[CONSTRUCTOR_FINE_EQUALS];
		fine += input[CONSTRUCTOR_FINE_COLL];
		fine += input[CONSTRUCTOR_FINE];
		fine += input[COARSE_FINE_EQUALS_COLL];
		fine += input[COARSE_FINE_EQUALS];
		fine += input[COARSE_FINE_COLL];
		fine += input[COARSE_FINE];
		fine += input[FINE_EQUALS_COLL];
		fine += input[FINE_EQUALS];
		fine += input[FINE_COLL];
		fine += input[FINE];

		equals += input[CONSTRUCTOR_COARSE_FINE_EQUALS_COLL];
		equals += input[CONSTRUCTOR_COARSE_FINE_EQUALS];
		equals += input[CONSTRUCTOR_COARSE_EQUALS_COLL];
		equals += input[CONSTRUCTOR_COARSE_EQUALS];
		equals += input[CONSTRUCTOR_FINE_EQUALS_COLL];
		equals += input[CONSTRUCTOR_FINE_EQUALS];
		equals += input[CONSTRUCTOR_EQUALS_COLL];
		equals += input[CONSTRUCTOR_EQUALS];
		equals += input[COARSE_FINE_EQUALS_COLL];
		equals += input[COARSE_FINE_EQUALS];
		equals += input[COARSE_EQUALS_COLL];
		equals += input[COARSE_EQUALS];
		equals += input[FINE_EQUALS_COLL];
		equals += input[FINE_EQUALS];
		equals += input[EQUALS_COLL];
		equals += input[EQUALS];

		collection += input[CONSTRUCTOR_COARSE_FINE_EQUALS_COLL];
		collection += input[CONSTRUCTOR_COARSE_FINE_COLL];
		collection += input[CONSTRUCTOR_COARSE_EQUALS_COLL];
		collection += input[CONSTRUCTOR_COARSE_COLL];
		collection += input[CONSTRUCTOR_FINE_EQUALS_COLL];
		collection += input[CONSTRUCTOR_FINE_COLL];
		collection += input[CONSTRUCTOR_EQUALS_COLL];
		collection += input[CONSTRUCTOR_COLL];
		collection += input[COARSE_FINE_EQUALS_COLL];
		collection += input[COARSE_FINE_COLL];
		collection += input[COARSE_EQUALS_COLL];
		collection += input[COARSE_COLL];
		collection += input[FINE_EQUALS_COLL];
		collection += input[FINE_COLL];
		collection += input[EQUALS_COLL];
		collection += input[COLL];

		total = immutable + input[NONE];

		totals[0] += constructor;
		totals[1] += coarse;
		totals[2] += fine;
		totals[3] += equals;
		totals[4] += collection;
		totals[5] += immutable;
		totals[6] += total;
	}
}