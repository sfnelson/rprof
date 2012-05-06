package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.Map;

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
import nz.ac.vuw.ecs.rprofs.server.domain.ClassSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.reports.ClassMapReduce;
import nz.ac.vuw.ecs.rprofs.server.reports.FieldMapReduce;
import org.slf4j.LoggerFactory;

import static nz.ac.vuw.ecs.rprofs.server.domain.ClassSummary.*;

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
		final String target = req.getParameter("target");

		final Dataset dataset = datasets.findDataset(handle);
		if (dataset != null) {
			try {
				Context.setDataset(dataset);

				if ("classes".equals(target) || "all".equals(target)) {
					final ClassMapReduce cmr = new ClassMapReduce(db.getClazzQuery());

					if (op == null || op.equals("mapreduce")) {
						db.createMapReduce(Instance.class, ClassSummary.class, cmr).run();
					}
					if (op == null || op.equals("summary")) {
						summary(resp);
						return;
					}
					if ("print".equals(op)) {
						print(resp);
						return;
					}
				}

				if ("fields".equals(target) || "all".equals(target)) {
					final FieldMapReduce fmr = new FieldMapReduce(db.getFieldQuery());

					if (op == null || op.equals("mapreduce")) {
						db.createMapReduce(Instance.class, FieldSummary.class, fmr).run();
					}
					if (op == null || "summary".equals(op)) {
						fields(dataset, resp);
						return;
					}
				}
			} finally {
				Context.clear();
			}
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

		Query.Cursor<? extends ClassSummary> query = db.getClassSummaryQuery().find();
		while (query.hasNext()) {
			ClassSummary r = query.next();
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

		int[] eqcol = new int[13];
		int[] eq = new int[13];
		int[] col = new int[13];
		int[] none = new int[13];

		Query.Cursor<? extends ClassSummary> query = db.getClassSummaryQuery().find();
		while (query.hasNext()) {
			ClassSummary r = query.next();

			Map<FieldId, FieldInfo> fields = r.getFields();
			int core0 = 0;
			int core1 = 0;
			int coreN = 0;
			for (FieldInfo field : fields.values()) {
				if (field.getMutable() == 0) {
					core0++;
					if (field.getReads() > 0) {
						core1++;
					}
					if (field.getReads() >= r.getNumObjects()) {
						coreN++;
					}
				}
			}

			boolean half = (core0 >= Math.max(1, fields.size() / 2));
			boolean full = (core0 >= Math.max(1, fields.size()));

			// todo is requiring at least one field sensible?
			if (fields.size() == 0) {
				half = false;
				full = false;
			}

			if (full && !half) {
				throw new RuntimeException("wat?");
			}

			getTotals(eqcol, r.getEqCol(), core0, core1, coreN, half, full);
			getTotals(eq, r.getEq(), core0, core1, coreN, half, full);
			getTotals(col, r.getCol(), core0, core1, coreN, half, full);
			getTotals(none, r.getNone(), core0, core1, coreN, half, full);
		}

		out.println("Set,Constructor,Coarse,Fine,Equals,Collection,Immutable,Total,"
				+ "Partial(n),Partial(1),Partial(0),Combined,Semi-Immutable,Fully-Immutable");

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

	private void fields(Dataset dataset, HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		ServletOutputStream out = resp.getOutputStream();

		int total = 0;
		int numDSCF = 0;
		int numDSC = 0;
		int numDSF = 0;
		int numDS = 0;
		int numDCF = 0;
		int numDC = 0;
		int numDF = 0;
		int numDNone = 0;
		int numSCF = 0;
		int numSC = 0;
		int numSF = 0;
		int numS = 0;
		int numCF = 0;
		int numC = 0;
		int numF = 0;
		int numNone = 0;

		Query.Cursor<? extends FieldSummary> query = db.getFieldSummaryQuery().find();
		while (query.hasNext()) {
			FieldSummary r = query.next();

			total++;
			if (false) ;
			else if (r.isDeclaredFinal() && r.isStationary() && r.isConstructed() && r.isFinal()) numDSCF++;
			else if (r.isDeclaredFinal() && r.isStationary() && r.isConstructed()) numDSC++;
			else if (r.isDeclaredFinal() && r.isStationary() && r.isFinal()) numDSF++;
			else if (r.isDeclaredFinal() && r.isStationary()) numDS++;
			else if (r.isDeclaredFinal() && r.isConstructed() && r.isFinal()) numDCF++;
			else if (r.isDeclaredFinal() && r.isConstructed()) numDC++;
			else if (r.isDeclaredFinal() && r.isFinal()) numDF++;
			else if (r.isDeclaredFinal()) numDNone++;
			else if (r.isStationary() && r.isConstructed() && r.isFinal()) numSCF++;
			else if (r.isStationary() && r.isConstructed()) numSC++;
			else if (r.isStationary() && r.isFinal()) numSF++;
			else if (r.isStationary()) numS++;
			else if (r.isConstructed() && r.isFinal()) numCF++;
			else if (r.isConstructed()) numC++;
			else if (r.isFinal()) numF++;
			else numNone++;
		}

		out.println(String.format("%s,%d," + "%d,%d,%d,%d,%d,%d,%d,%d," + "%d,%d,%d,%d,%d,%d,%d,%d",
				dataset.getBenchmark(), total,
				numDSCF, numDSC, numDSF, numDS, numDCF, numDC, numDF, numDNone,
				numSCF, numSC, numSF, numS, numCF, numC, numF, numNone));

		resp.getOutputStream().close();
	}

	private void getTotals(int[] totals, int[] input, int core0, int core1, int coreN, boolean half, boolean full) {
		int constructor = 0;
		int fine = 0;
		int coarse = 0;
		int equals = 0;
		int collection = 0;
		int immutable = 0;
		int partial0 = 0;
		int partial1 = 0;
		int partialN = 0;
		int combined = 0;
		int semiImmutable = 0;
		int fullyImmutable = 0;
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

		if (coreN > 0) {
			partialN = immutable + input[NONE];
		} else if (core1 > 0) {
			partial1 = immutable + input[NONE];
		} else if (core0 > 0) {
			partial0 = immutable + input[NONE];
		}

		if (core0 > 0) {
			combined = input[NONE];
		}

		if (half) {
			semiImmutable = immutable + input[NONE];
		}

		if (full) {
			fullyImmutable = immutable + input[NONE];
		}

		totals[0] += constructor;
		totals[1] += coarse;
		totals[2] += fine;
		totals[3] += equals;
		totals[4] += collection;
		totals[5] += immutable;
		totals[6] += total;
		totals[7] += partialN;
		totals[8] += partial1;
		totals[9] += partial0;
		totals[10] += combined;
		totals[11] += semiImmutable;
		totals[12] += fullyImmutable;
	}
}