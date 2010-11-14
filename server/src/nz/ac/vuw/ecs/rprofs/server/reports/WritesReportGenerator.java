/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Entry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.Database;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.server.reports.WritesReport.ClassReport;
import nz.ac.vuw.ecs.rprofs.server.reports.WritesReport.InstanceReport;
import nz.ac.vuw.ecs.rprofs.server.reports.WritesReport.PackageReport;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class WritesReportGenerator extends ReportGenerator implements Report.EntryVisitor<ArrayList<? extends Report.Entry>> {

	protected WritesReportGenerator(Context run, Database database) {
		super(run, database);
	}

	private final Map<String, PackageReport> packages = Collections.newMap();
	private final Map<Integer, ClassReport> classes = Collections.newMap();
	private final Map<Long, InstanceReport> instances = Collections.newMap();

	private final Map<Integer, ClassRecord> classMap = Collections.newMap();

	private MethodRecord getMethodRecord(int cnum, int mnum) {
		ClassRecord cr = classMap.get(cnum);
		if (cr == null) {
			return null;
		}
		List<MethodRecord> methods = cr.getMethods();
		if (mnum - 1 < methods.size() && mnum - 1 >= 0) {
			return methods.get(mnum - 1);
		}
		return null;
	}
	
	@Override
	protected void reset() {
		packages.clear();
		classes.clear();
		instances.clear();
		classMap.clear();
	}

	@Override
	protected void run() throws DatabaseNotAvailableException {
		Status status = getStatus();

		status.progress = 0;
		status.stage = "Processing classes (1/3)";
		status.limit = getContext().getClasses().size();
		for (ClassRecord cr: getContext().getClasses()) {
			PackageReport pkg = packages.get(cr.getPackage());
			if (pkg == null) {
				pkg = WritesReport.create(cr.getPackage());
				packages.put(cr.getPackage(), pkg);
			}

			ClassReport cls = WritesReport.create(cr);
			cls.classes = 1;
			pkg.addChild(cls);
			classes.put(cr.getId(), cls);
			classMap.put(cr.getId(), cr);

			status.progress++;
		}

		status.progress = 0;
		status.stage = "Processing allocations (2/3)";
		status.limit = getDB().getNumLogs(getRun(), LogRecord.ALLOCATION, 0);
		for (LogRecord lr: getDB().getLogs(getRun(), 0, status.limit, LogRecord.ALLOCATION, 0)) {
			long id = lr.getArguments()[0];
			if (!instances.containsKey(id)) {
				InstanceReport ir = WritesReport.create(id);
				ir.instances = 1;
				ClassReport cr = classes.get(lr.getClassNumber());
				if (cr != null) {
					cr.addChild(ir);
				}
				instances.put(id, ir);
			}
			status.progress++;
		}

		status.progress = 0;
		status.stage = "Processing events (3/3)";
		status.limit = getDB().getNumLogs(getRun(), LogRecord.METHODS | LogRecord.FIELDS, 0);
		for (LogRecord lr: getDB().getLogs(getRun(), 0, status.limit, LogRecord.METHODS | LogRecord.FIELDS, 0)) {
			if (lr.getArguments().length > 0) {
				InstanceReport ir = instances.get(lr.getArguments()[0]);
				MethodRecord mr = getMethodRecord(lr.getClassNumber(), lr.getMethodNumber());
				if (ir != null) {
					switch (lr.getEvent()) {
					case LogRecord.METHOD_ENTER:
						if (mr != null && (mr.isEquals() || mr.isHashCode())) {
							ir.equalsCalled = true;
						}
					case LogRecord.METHOD_EXCEPTION:
					case LogRecord.METHOD_RETURN:
						if (mr != null && mr.isInit()) {
							ir.constructor = 0;
							ir.read = 0;
							ir.fieldRead = false;
						}
						break;
					case LogRecord.FIELD_WRITE:
						ir.writes++;
						ir.constructor++;
						if (ir.fieldRead) ir.read++;
						if (ir.equalsCalled) ir.equals++;
						break;
					case LogRecord.FIELD_READ:
						ir.fieldRead = true;
						break;
					}
				}
			}

			status.progress++;
		}
	}

	private ArrayList<PackageEntry> getPackages() {
		ArrayList<PackageEntry> result = Collections.newList();
		for (PackageReport r: packages.values()) {
			result.add(r.toEntry());
		}
		return result;
	}

	@Override
	public ArrayList<ClassEntry> visitPackageEntry(PackageEntry entry) {
		ArrayList<ClassEntry> result = Collections.newList();
		for (ClassReport r: packages.get(entry.pkg).getChildren()) {
			result.add(r.toEntry());
		}
		return result;
	}

	@Override
	public ArrayList<InstanceEntry> visitClassEntry(ClassEntry entry) {
		ArrayList<InstanceEntry> result = Collections.newList();
		for (InstanceReport r: classes.get(entry.cls.id).getChildren()) {
			result.add(r.toEntry());
		}
		return result;
	}

	@Override
	public ArrayList<? extends Entry> visitInstanceEntry(InstanceEntry entry) {
		return Collections.newList();
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

	private static final ReportFactory factory = new ReportFactory() {

		private Report report;

		{
			report = new Report("writes", "Writes", "A list of classes showing writes to fields after particular events.", 7);
			report.headings[0] = "Package";
			report.types[0] = Report.Type.NAME;
			report.headings[1] = "Class";
			report.types[1] = Report.Type.NAME;
			report.headings[2] = "Instance";
			report.types[2] = Report.Type.NAME;
			report.headings[3] = "Total";
			report.headingTitle[3] = "Total writes to fields";
			report.types[3] = Report.Type.COUNT;
			report.headings[4] = "Constructor";
			report.headingTitle[4] = "Writes to fields after constructor";
			report.types[4] = Report.Type.COUNT;
			report.headings[5] = "Read";
			report.headingTitle[5] = "Writes to fields after first read after constructor";
			report.types[5] = Report.Type.COUNT;
			report.headings[6] = "Equals";
			report.headingTitle[6] = "Writes to fields after first call to equals or hashcode";
			report.types[6] = Report.Type.COUNT;
		}

		@Override
		public Report getReport() {
			return report;
		}

		@Override
		public ReportGenerator createGenerator(Database db, Context run) {
			return new WritesReportGenerator(run, db);
		}
	};

	public static ReportFactory getFactory() {
		return factory;
	}
}
