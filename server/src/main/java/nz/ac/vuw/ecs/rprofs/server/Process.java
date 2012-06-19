package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;
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
						classes(resp);
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

				if ("objects".equals(target)) {
					final ClassMapReduce cmr = new ClassMapReduce(db.getClazzQuery());

					if (op == null || op.equals("mapreduce")) {
						db.createMapReduce(Instance.class, ClassSummary.class, cmr).run();
					}
					if (op == null || "summary".equals(op)) {
						objects(dataset, resp);
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

	private static class ClassData implements Comparable<ClassData> {
		final int numFields;
		int numClasses;
		int numObjects;
		int numImmClasses;
		int numMutClasses;
		int numImmObjects;
		int numMutObjects;

		public ClassData(int numFields) {
			this.numFields = numFields;
		}

		public String toString() {
			return String.format("%d,%d,%d,%d,%d,%d,%d", numFields,
					numClasses, numImmClasses, numMutClasses,
					numObjects, numImmObjects, numMutObjects);
		}

		@Override
		public int compareTo(ClassData o) {
			return numFields - o.numFields;
		}
	}

	private void classes(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		ServletOutputStream out = resp.getOutputStream();

		Map<Integer, ClassData> data = Maps.newTreeMap();

		Query.Cursor<? extends ClassSummary> query = db.getClassSummaryQuery().find();
		while (query.hasNext()) {
			ClassSummary r = query.next();

			Map<FieldId, FieldInfo> fields = r.getFields();
			ClassData c = data.get(fields.size());
			if (c == null) {
				c = new ClassData(fields.size());
				data.put(fields.size(), c);
			}

			boolean classFullyImmutable = true;
			boolean classFullyMutable = true;
			for (FieldInfo field : fields.values()) {
				if (field.getMutable() == 0) {
					classFullyMutable = false;
				} else {
					classFullyImmutable = false;
				}
			}
			if (classFullyImmutable) classFullyMutable = false;
			c.numClasses += 1;
			if (classFullyImmutable) c.numImmClasses += 1;
			if (classFullyMutable) c.numMutClasses += 1;
			c.numObjects += r.getNumObjects();
			c.numImmObjects += r.getNumFullyImmutable();
			c.numMutObjects += r.getNumFullyMutable();
		}

		out.println("Fields,Classes,ImmClasses,MutClasses,Objects,ImmObjects,MutObjects");

		for (ClassData c : data.values()) {
			out.println(c.toString());
		}

		resp.getOutputStream().close();
	}

	private void addObject(int[] out, int[] in) {
		out[0] += in[CONSTRUCTOR_COARSE_FINE_EQUALS_COLL];
		out[0] += in[CONSTRUCTOR_COARSE_FINE_EQUALS];
		out[0] += in[CONSTRUCTOR_COARSE_FINE_COLL];
		out[0] += in[CONSTRUCTOR_COARSE_FINE];

		out[1] += in[CONSTRUCTOR_COARSE_EQUALS_COLL];
		out[1] += in[CONSTRUCTOR_COARSE_EQUALS];
		out[1] += in[CONSTRUCTOR_COARSE_COLL];
		out[1] += in[CONSTRUCTOR_COARSE];

		out[2] += in[CONSTRUCTOR_FINE_EQUALS_COLL];
		out[2] += in[CONSTRUCTOR_FINE_EQUALS];
		out[2] += in[CONSTRUCTOR_FINE_COLL];
		out[2] += in[CONSTRUCTOR_FINE];

		out[3] += in[CONSTRUCTOR_EQUALS_COLL];
		out[3] += in[CONSTRUCTOR_EQUALS];
		out[3] += in[CONSTRUCTOR_COLL];
		out[3] += in[CONSTRUCTOR];

		out[4] += in[COARSE_FINE_EQUALS_COLL];
		out[4] += in[COARSE_FINE_EQUALS];
		out[4] += in[COARSE_FINE_COLL];
		out[4] += in[COARSE_FINE];

		out[5] += in[COARSE_EQUALS_COLL];
		out[5] += in[COARSE_EQUALS];
		out[5] += in[COARSE_COLL];
		out[5] += in[COARSE];

		out[6] += in[FINE_EQUALS_COLL];
		out[6] += in[FINE_EQUALS];
		out[6] += in[FINE_COLL];
		out[6] += in[FINE];

		out[7] += in[EQUALS_COLL];
		out[7] += in[EQUALS];
		out[7] += in[COLL];
		out[7] += in[NONE];

		out[8] += in[FINE_EQUALS];

		out[8] += in[CONSTRUCTOR_COARSE_FINE_EQUALS_COLL];
		out[8] += in[CONSTRUCTOR_COARSE_FINE_EQUALS];
		out[8] += in[CONSTRUCTOR_FINE_EQUALS_COLL];
		out[8] += in[CONSTRUCTOR_FINE_EQUALS];
		out[8] += in[COARSE_FINE_EQUALS_COLL];
		out[8] += in[COARSE_FINE_EQUALS];
		out[8] += in[FINE_EQUALS_COLL];
		out[8] += in[FINE_EQUALS];

		out[9] += in[CONSTRUCTOR_COARSE_EQUALS_COLL];
		out[9] += in[CONSTRUCTOR_COARSE_EQUALS];
		out[9] += in[CONSTRUCTOR_EQUALS_COLL];
		out[9] += in[CONSTRUCTOR_EQUALS];
		out[9] += in[COARSE_EQUALS_COLL];
		out[9] += in[COARSE_EQUALS];
		out[9] += in[EQUALS_COLL];
		out[9] += in[EQUALS];

		out[10] += in[CONSTRUCTOR_COARSE_FINE_EQUALS_COLL];
		out[10] += in[CONSTRUCTOR_COARSE_FINE_COLL];
		out[10] += in[CONSTRUCTOR_FINE_EQUALS_COLL];
		out[10] += in[CONSTRUCTOR_FINE_COLL];
		out[10] += in[COARSE_FINE_EQUALS_COLL];
		out[10] += in[COARSE_FINE_COLL];
		out[10] += in[FINE_EQUALS_COLL];
		out[10] += in[FINE_COLL];

		out[11] += in[CONSTRUCTOR_COARSE_EQUALS_COLL];
		out[11] += in[CONSTRUCTOR_COARSE_COLL];
		out[11] += in[CONSTRUCTOR_EQUALS_COLL];
		out[11] += in[CONSTRUCTOR_COLL];
		out[11] += in[COARSE_EQUALS_COLL];
		out[11] += in[COARSE_COLL];
		out[11] += in[EQUALS_COLL];
		out[11] += in[COLL];
	}

	private String printObject(String name, int[] counts) {
		StringBuilder b = new StringBuilder();
		b.append(name);
		for (int i = 0; i < counts.length; i++) {
			b.append(',');
			b.append(counts[i]);
		}
		return b.toString();
	}

	private void objects(Dataset dataset, HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		ServletOutputStream out = resp.getOutputStream();

		final int SIZE = 12;

		int[] all = new int[SIZE];
		int[] nosys = new int[SIZE];
		int[] eqcol = new int[SIZE];
		int[] eq = new int[SIZE];
		int[] col = new int[SIZE];
		int[] none = new int[SIZE];

		Query.Cursor<? extends ClassSummary> query = db.getClassSummaryQuery().find();
		while (query.hasNext()) {
			ClassSummary r = query.next();

			addObject(all, r.getEqCol());
			addObject(all, r.getEq());
			addObject(all, r.getCol());
			addObject(all, r.getNone());

			if (r.getClassName() == null || r.getClassName().startsWith("java")) {
				addObject(nosys, r.getEqCol());
				addObject(nosys, r.getEq());
				addObject(nosys, r.getCol());
				addObject(nosys, r.getNone());
			}

			addObject(eqcol, r.getEqCol());
			addObject(eq, r.getEq());
			addObject(col, r.getCol());
			addObject(none, r.getNone());
		}

		out.println("Set,CoSfS,CoS,CfS,C,oSfS,oS,fS,None,fSE,E,fSK,K");

		out.println(printObject("All", all));
		out.println(printObject("NoSys", nosys));
		out.println(printObject("EqCol", eqcol));
		out.println(printObject("Eq", eq));
		out.println(printObject("Col", col));
		out.println(printObject("None", none));

		resp.getOutputStream().close();
	}

	public static final int FIELD_DSCF = 0;
	public static final int FIELD_DSC = 1;
	public static final int FIELD_DSF = 2;
	public static final int FIELD_DS = 3;
	public static final int FIELD_DCF = 4;
	public static final int FIELD_DC = 5;
	public static final int FIELD_DF = 6;
	public static final int FIELD_D = 7;
	public static final int FIELD_SCF = 8;
	public static final int FIELD_SC = 9;
	public static final int FIELD_SF = 10;
	public static final int FIELD_S = 11;
	public static final int FIELD_CF = 12;
	public static final int FIELD_C = 13;
	public static final int FIELD_F = 14;
	public static final int FIELD_NONE = 15;

	private void addField(int[] counts, FieldSummary r) {
		if (r.isDeclaredFinal() && r.isStationary() && r.isConstructed() && r.isFinal())
			counts[FIELD_DSCF]++;
		else if (r.isDeclaredFinal() && r.isStationary() && r.isConstructed())
			counts[FIELD_DSC]++;
		else if (r.isDeclaredFinal() && r.isStationary() && r.isFinal())
			counts[FIELD_DSF]++;
		else if (r.isDeclaredFinal() && r.isStationary())
			counts[FIELD_DS]++;
		else if (r.isDeclaredFinal() && r.isConstructed() && r.isFinal())
			counts[FIELD_DCF]++;
		else if (r.isDeclaredFinal() && r.isConstructed())
			counts[FIELD_DC]++;
		else if (r.isDeclaredFinal() && r.isFinal())
			counts[FIELD_DF]++;
		else if (r.isDeclaredFinal())
			counts[FIELD_D]++;
		else if (r.isStationary() && r.isConstructed() && r.isFinal())
			counts[FIELD_SCF]++;
		else if (r.isStationary() && r.isConstructed())
			counts[FIELD_SC]++;
		else if (r.isStationary() && r.isFinal())
			counts[FIELD_SF]++;
		else if (r.isStationary())
			counts[FIELD_S]++;
		else if (r.isConstructed() && r.isFinal())
			counts[FIELD_CF]++;
		else if (r.isConstructed())
			counts[FIELD_C]++;
		else if (r.isFinal())
			counts[FIELD_F]++;
		else
			counts[FIELD_NONE]++;
	}

	private String printField(String name, int total, int[] counts) {
		StringBuilder b = new StringBuilder();
		b.append(name);
		b.append(',');
		b.append(total);
		for (int i = 0; i < counts.length; i++) {
			b.append(',');
			b.append(counts[i]);
		}
		return b.toString();
	}

	private void fields(Dataset dataset, HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		ServletOutputStream out = resp.getOutputStream();

		int numAll = 0;
		int[] all = new int[16];

		int numNoSystem = 0;
		int[] noSystem = new int[16];

		int numPrim = 0;
		int[] prim = new int[16];

		int numPrimNoSystem = 0;
		int[] primNoSystem = new int[16];

		int numRef = 0;
		int[] ref = new int[16];

		int numRefNoSystem = 0;
		int[] refNoSystem = new int[16];

		Query.Cursor<? extends FieldSummary> query = db.getFieldSummaryQuery().find();
		while (query.hasNext()) {
			FieldSummary r = query.next();

			numAll++;
			addField(all, r);

			if (r.getDescription().startsWith("L")) {
				numRef++;
				addField(ref, r);
			} else {
				numPrim++;
				addField(prim, r);
			}

			if (!r.getPackageName().startsWith("sun.")
					&& !r.getPackageName().startsWith("java.")
					&& !r.getPackageName().startsWith("javax.")) {
				numNoSystem++;
				addField(noSystem, r);

				if (r.getDescription().startsWith("L")) {
					numRefNoSystem++;
					addField(refNoSystem, r);
				} else {
					numPrimNoSystem++;
					addField(primNoSystem, r);
				}
			}
		}

		out.println("Set,Total,DSCF,DSC,DSF,DS,DCF,DC,DF,D,SCF,SC,SF,S,CF,C,F,None");
		out.println(printField("All", numAll, all));
		out.println(printField("Prim", numPrim, prim));
		out.println(printField("Ref", numRef, ref));
		out.println(printField("NoSystem", numNoSystem, noSystem));
		out.println(printField("PrimNS", numPrimNoSystem, primNoSystem));
		out.println(printField("RefNS", numRefNoSystem, refNoSystem));

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