/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Entry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.reports.EqualsReport.ClassReport;
import nz.ac.vuw.ecs.rprofs.server.reports.EqualsReport.FieldReport;
import nz.ac.vuw.ecs.rprofs.server.reports.EqualsReport.PackageReport;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class EqualsReportGenerator extends ReportGenerator implements Report.EntryVisitor<ArrayList<? extends Report.Entry>> {

	protected EqualsReportGenerator(Context run, Dataset database) {
		super(run, database);
	}

	@Override
	public ArrayList<? extends Entry> getReportData(Entry parent) {
		if (parent == null) {
			return getPackages();
		}
		else {
			return parent.visit(this);
		}
	}

	private ArrayList<PackageEntry> getPackages() {
		ArrayList<PackageEntry> result = Collections.newList();
		for (PackageReport pr: packages.values()) {
			result.add(pr.toEntry());
		}
		return result;
	}

	@Override
	public ArrayList<ClassEntry> visitPackageEntry(PackageEntry entry) {
		ArrayList<ClassEntry> result = Collections.newList();

		PackageReport pr = packages.get(entry.pkg);
		for (ClassReport cr: pr.getChildren()) {
			result.add(cr.toEntry());
		}

		return result;
	}

	@Override
	public ArrayList<InstanceEntry> visitClassEntry(ClassEntry entry) {
		ArrayList<InstanceEntry> result = Collections.newList();

		ClassReport cr = classes.get(entry.cls.id);
		for (FieldReport fr: cr.getChildren()) {
			result.add(fr.toEntry());
		}

		return result;
	}

	@Override
	public ArrayList<? extends Entry> visitInstanceEntry(InstanceEntry entry) {
		return Collections.newList();
	}

	private final Map<String, PackageReport> packages = Collections.newMap();
	private final Map<Integer, ClassReport> classes = Collections.newMap();

	private final Map<Integer, Class> classMap = Collections.newMap();

	@Override
	protected void reset() {
		packages.clear();
		classes.clear();
		classMap.clear();
	}

	@Override
	protected void run() throws DatabaseNotAvailableException {
		Status status = getStatus();

		Map<String, PackageReport> packages = Collections.newMap();
		Map<Integer, ClassReport> classes = Collections.newMap();

		status.limit = getContext().getClasses().size();
		status.progress = 0;
		status.stage = "Processing Class Records (1/2)";
		for (Class cr: getContext().getClasses()) {
			PackageReport pkg = packages.get(cr.getPackage());

			if (pkg == null) {
				pkg = EqualsReport.create(cr.getPackage());
				packages.put(cr.getPackage(), pkg);
			}

			ClassReport cls = EqualsReport.create(cr);
			cls.classes = 1;

			classes.put(cr.getClassId(), cls);
			classMap.put(cr.getClassId(), cr);

			status.progress++;
		}

		int flags = Event.METHODS | Event.FIELD_READ;
		status.limit = getDB().getNumLogs(flags);
		status.progress = 0;
		status.stage = "Processing Logs (2/2)";

		Map<Long, Stack<Method>> stacks = Collections.newMap();
		for (Event lr: getDB().getLogs(0, status.limit, flags)) {
			Stack<Method> stack = stacks.get(lr.getThread());
			if (stack == null) {
				stack = Collections.newStack();
				stacks.put(lr.getThread(), stack);
			}

			Class cr = null;
			Method mr = null;
			Field fr = null;

			cr = classMap.get(lr.getClassNumber());
			if (cr != null && lr.getMethodNumber() > 0 && cr.getMethods().size() >= lr.getMethodNumber()) {
				mr = cr.getMethods().get(lr.getMethodNumber() - 1);
			}

			switch (lr.getEvent()) {
			case Event.METHOD_ENTER:
				if (mr != null && (mr.isEquals() || mr.isHashCode())) {
					if (lr.getArguments()[0] == 0) {
						System.out.println("null <this>: ->" + mr); break;
					}
					stack.push(mr);
					this.classes.put(cr.getClassId(), classes.get(cr.getClassId()));
					PackageReport pr = packages.get(cr.getPackage());
					pr.addChild(classes.get(cr.getClassId()));
					this.packages.put(cr.getPackage(), pr);
				}
				break;
			case Event.METHOD_EXCEPTION:
			case Event.METHOD_RETURN:
				String type = (lr.getEvent() == Event.METHOD_EXCEPTION) ? "</" : "<-";
				if (mr != null && (mr.isEquals() || mr.isHashCode())) {
					if (lr.getArguments()[0] == 0) {
						System.out.println("null <this>: " + type + mr); break;
					}
					if (stack.isEmpty() || mr != stack.peek()) {
						throw new RuntimeException("return was not matched! " + type + mr +
								" - " + lr.getArguments()[0] + " - " + lr.getThread());
					}
					else {
						stack.pop();
					}
				}
				break;
			case Event.FIELD_READ:
				if (stack.isEmpty()) break;
				if (cr != null && lr.getMethodNumber() > 0 && cr.getFields().size() >= lr.getMethodNumber()) {
					fr = cr.getFields().get(lr.getMethodNumber() - 1);
				}
				if (fr != null) {
					for (Method m: stack) {
						ClassReport c = classes.get(m.parent.getClassId());
						FieldReport f = c.fields.get(fr);
						if (f == null) {
							f = EqualsReport.create(fr);
							c.addChild(f);
							c.fields.put(fr, f);
						}
						if (m.isEquals()) {
							f.equals = 1;
							f.fields = 1;
						}
						else {
							f.hash = 1;
							f.fields = 1;
						}
					}
				}
				break;
			}

			status.progress++;
		}

		for (Stack<Method> stack: stacks.values()) {
			if (!stack.isEmpty()) {
				System.err.println("error: stack was not empty when we finished");
				System.err.println(stack);
			}
		}
	}

	private static final ReportFactory factory = new ReportFactory() {

		private final Report report;

		{
			report = new Report("fields", "Fields", "A list of fields used in equals and hashcode methods", 5);
			report.headings[0] = "Package";
			report.headings[1] = "Class";
			report.headings[2] = "Field";
			report.headings[3] = "Equals";
			report.headings[4] = "Hash";
			report.types[0] = Report.Type.NAME;
			report.types[1] = Report.Type.NAME;
			report.types[2] = Report.Type.NAME;
			report.types[3] = Report.Type.COUNT;
			report.types[4] = Report.Type.COUNT;
		}

		@Override
		public ReportGenerator createGenerator(Dataset db, Context run) {
			return new EqualsReportGenerator(run, db);
		}

		@Override
		public Report getReport() {
			return report;
		}

	};

	public static ReportFactory getFactory() {
		return factory;
	}
}
