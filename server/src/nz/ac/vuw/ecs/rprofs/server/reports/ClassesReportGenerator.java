/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.ArrayList;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Entry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.EntryVisitor;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.Database;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.server.reports.ClassesReport.ClassReport;
import nz.ac.vuw.ecs.rprofs.server.reports.ClassesReport.MethodReport;
import nz.ac.vuw.ecs.rprofs.server.reports.ClassesReport.PackageReport;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ClassesReportGenerator extends ReportGenerator implements EntryVisitor<ArrayList<? extends Report.Entry>> {

	public ClassesReportGenerator(Context run, Database db) {
		super(run, db);
	}

	@Override
	public ArrayList<? extends Entry> getReportData(Entry parent) {
		if (parent == null) {
			return getPackageResults();
		}
		else {
			return parent.visit(this);
		}
	}

	private ArrayList<PackageEntry> getPackageResults() {
		ArrayList<PackageEntry> entries = Collections.newList();
		for (PackageReport pr: packages.values()) {
			entries.add(pr.toEntry());
		}

		return entries;
	}

	@Override
	public ArrayList<ClassEntry> visitPackageEntry(PackageEntry entry) {
		PackageReport pr = packages.get(entry.pkg);

		ArrayList<ClassEntry> entries = Collections.newList();
		for (ClassReport cr: pr.getChildren()) {
			entries.add(cr.toEntry());
		}

		return entries;
	}

	@Override
	public ArrayList<InstanceEntry> visitClassEntry(ClassEntry entry) {
		ClassReport cr = classes.get(entry.cls.id);

		ArrayList<InstanceEntry> entries = Collections.newList();
		for (MethodReport mr: cr.getChildren()) {
			entries.add(mr.toEntry());
		}

		return entries;
	}

	@Override
	public ArrayList<? extends Entry> visitInstanceEntry(InstanceEntry entry) {
		return Collections.newList();
	}

	private Map<String, PackageReport> packages = Collections.newMap();
	private Map<Integer, ClassReport> classes = Collections.newMap();

	@Override
	protected void reset() {
		packages.clear();
		classes.clear();
	}
	
	@Override
	protected void run() throws DatabaseNotAvailableException {
		Status status = getStatus();

		status.limit = getContext().getClasses().size();
		status.progress = 0;
		status.stage = "Processing Class Records";
		for (ClassRecord cr: getContext().getClasses()) {
			PackageReport pkg = packages.get(cr.getPackage());

			if (pkg == null) {
				pkg = ClassesReport.create(cr.getPackage());
				packages.put(cr.getPackage(), pkg);
			}

			ClassReport cls = ClassesReport.create(cr);
			cls.classes = 1;
			cls.methods = 0;
			cls.flags = cr.getFlags();
			pkg.addChild(cls);

			classes.put(cr.getId(), cls);

			for (MethodRecord mr: cr.getMethods()) {
				MethodReport method = ClassesReport.create(mr);
				method.classes = 0;
				method.methods = 1;
				if (mr.isEquals()) {
					method.equals = 1;
				}
				if (mr.isHashCode()) {
					method.hash = 1;
				}
				cls.addChild(method);
			}

			status.progress++;
		}
	}

	private static ReportFactory factory = new ReportFactory() {

		private Report report;

		{
			report = new Report("classes", "Classes", "A list of classes woven by the profiler.", 6);
			report.headings[0] = "Package";
			report.types[0] = Report.Type.NAME;
			report.headings[1] = "Class";
			report.types[1] = Report.Type.NAME;
			report.headings[2] = "Method";
			report.types[2] = Report.Type.NAME;
			report.headings[3] = "Flags";
			report.types[3] = Report.Type.FLAG;
			report.flags[3] = new String[] {
					"Class version updated",
					"Class ignored",
					"Special weaver used"	
			};
			report.headings[4] = "Equals";
			report.types[4] = Report.Type.COUNT;
			report.headings[5] = "Hash";
			report.types[5] = Report.Type.COUNT;
		}

		@Override
		public ReportGenerator createGenerator(Database db, Context run) {
			return new ClassesReportGenerator(run, db);
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
