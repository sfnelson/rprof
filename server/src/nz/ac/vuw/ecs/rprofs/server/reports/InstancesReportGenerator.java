/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.ArrayList;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Entry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.Database;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.server.data.InstanceRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.server.reports.InstancesReport.ClassReport;
import nz.ac.vuw.ecs.rprofs.server.reports.InstancesReport.InstanceReport;
import nz.ac.vuw.ecs.rprofs.server.reports.InstancesReport.PackageReport;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstancesReportGenerator extends ReportGenerator implements Report.EntryVisitor<ArrayList<? extends Report.Entry>> {

	private Map<String, PackageReport> packages = Collections.newMap();
	private Map<ClassRecord, ClassReport> classes = Collections.newMap();
	private Map<InstanceRecord, InstanceReport> instances = Collections.newMap();

	private Map<Integer, ClassRecord> classMap = Collections.newMap();
	private Map<Long, InstanceRecord> instanceMap = Collections.newMap();

	public InstancesReportGenerator(Context run, Database database) {
		super(run, database);
	}

	protected void reset() {
		packages.clear();
		classes.clear();
		instances.clear();
		classMap.clear();
		instanceMap.clear();
	}

	public void run() throws DatabaseNotAvailableException {
		Status status = getStatus();		

		status.stage = "Loading Class List (1/3)";
		status.progress = 0;
		status.limit = getContext().getClasses().size();
		for (ClassRecord cr: getContext().getClasses()) {
			PackageReport pkg = packages.get(cr.getPackage());
			if (pkg == null) {
				pkg = InstancesReport.create(cr.getPackage());
				packages.put(cr.getPackage(), pkg);
			}
			ClassReport cls = InstancesReport.create(cr);
			cls.classes = 1;
			classes.put(cr, cls);
			classMap.put(cr.getId(), cr);
			pkg.addChild(cls);
			status.progress++;
		}

		status.stage = "Processing Allocation Logs (2/3)";
		status.progress = 0;
		status.limit = getDB().getNumLogs(getRun(), LogData.ALLOCATION, 0);
		for (LogInfo lr: getDB().getLogs(getRun(), 0, status.limit, LogData.ALLOCATION, 0)) {
			ClassRecord cr = classMap.get(lr.getClassNumber());
			if (cr == null) continue;
			ClassReport cls = classes.get(cr);

			InstanceRecord ir = instanceMap.get(lr.getArguments()[0]);
			if (ir == null) {
				MethodRecord mr = null;
				if (lr.getMethodNumber() > 0) {
					mr = cr.getMethods().get(lr.getMethodNumber() - 1);
				}
				ir = new InstanceRecord(lr.getArguments()[0], cr, mr);
				instanceMap.put(lr.getArguments()[0], ir);
			}

			InstanceReport instance = instances.get(ir);
			if (instance == null) {
				instance = InstancesReport.create(ir.getId());
				instance.instances = 1;
				cls.addChild(instance);
				instances.put(ir, instance);
			}

			status.progress++;
		}

		status.stage = "Processing field accesses (3/3)";
		status.progress = 0;
		status.limit = getDB().getNumLogs(getRun(), LogData.FIELDS, 0);
		for (LogInfo lr: getDB().getLogs(getRun(), 0, status.limit, LogData.FIELDS, 0)) {
			InstanceRecord ir = null;
			ClassRecord cr = null;
			FieldRecord fr = null;

			if (lr.getArguments().length > 0) {
				ir = instanceMap.get(lr.getArguments()[0]);
			}
			cr = classMap.get(lr.getClassNumber());
			if (cr != null && lr.getMethodNumber() > 0 && cr.getFields().size() >= lr.getMethodNumber()) {
				fr = cr.getFields().get(lr.getMethodNumber() - 1);
			}

			if (ir == null || fr == null) {
				System.err.printf("error reporting on field access: %d:%d\n", lr.getClassNumber(), lr.getMethodNumber());
			}
			else if (lr.getEvent() == LogData.FIELD_READ) {
				instances.get(ir).reads++;
				if (fr.equals || fr.hash) {
					instances.get(ir).ereads++;
				}
			}
			else {
				instances.get(ir).writes++;
				if (fr.equals || fr.hash) {
					instances.get(ir).ewrites++;
				}
			}

			status.progress++;
		}
	}

	/**
	 * @return
	 */
	public ArrayList<? extends Entry> getPackageReports() {
		ArrayList<Report.PackageEntry> result = Collections.newList();
		for (PackageReport r: packages.values()) {
			result.add(r.toEntry());
		}
		return result;
	}

	@Override
	public ArrayList<ClassEntry> visitPackageEntry(PackageEntry entry) {
		ArrayList<ClassEntry> result = Collections.newList();

		PackageReport pkg = packages.get(entry.pkg);
		if (pkg == null) return result;
		for (ClassReport r: pkg.getChildren()) {
			result.add(r.toEntry());
		}
		return result;
	}


	@Override
	public ArrayList<InstanceEntry> visitClassEntry(ClassEntry entry) {
		ArrayList<InstanceEntry> result = Collections.newList();
		ClassRecord cr = classMap.get(entry.cls.id);
		if (cr == null) return result;
		ClassReport cls = classes.get(cr);
		for (InstanceReport r: cls.getChildren()) {
			result.add(r.toEntry());
		}
		return result;
	}

	@Override
	public ArrayList<? extends Entry> visitInstanceEntry(InstanceEntry entry) {
		return Collections.newList(); // nothing to return (at present)
	}

	@Override
	public ArrayList<? extends Entry> getReportData(Entry parent) {
		ArrayList<? extends Entry> result;

		if (parent == null) {
			result = getPackageReports();
		}
		else {
			result = parent.visit(this);
		}

		return result;
	}

	private static ReportFactory factory = new ReportFactory() {
		private Report report;
		{
			report = new Report("instances", "Instances", "A list of instances encountered, with counts of field reads and writes for each.", 7);
			report.headings[0] = "Package";
			report.types[0] = Report.Type.NAME;
			report.headings[1] = "Class";
			report.types[1] = Report.Type.NAME;
			report.headings[2] = "Instance";
			report.types[2] = Report.Type.OBJECT;
			report.headings[3] = "Reads";
			report.types[3] = Report.Type.COUNT;
			report.headings[4] = "Writes";
			report.types[4] = Report.Type.COUNT;
			report.headings[5] = "EReads";
			report.types[5] = Report.Type.COUNT;
			report.headings[6] = "EWrites";
			report.types[6] = Report.Type.COUNT;
		}
		@Override
		public ReportGenerator createGenerator(Database db, Context run) {
			return new InstancesReportGenerator(run, db);
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
