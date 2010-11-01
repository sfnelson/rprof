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
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.Database;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.server.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.server.reports.ProblemReport.ClassReport;
import nz.ac.vuw.ecs.rprofs.server.reports.ProblemReport.PackageReport;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ProblemReportGenerator extends ReportGenerator implements Report.EntryVisitor<ArrayList<? extends Report.Entry>> {

	protected ProblemReportGenerator(ProfilerRun run, Database database) {
		super(run, database);
	}

	private Map<String, PackageReport> packages = Collections.newMap();
	
	@Override
	public ArrayList<? extends Entry> getReportData(Entry parent) {
		if (parent == null) {
			return getPackageEntries();
		}
		else {
			return parent.visit(this);
		}
	}

	@Override
	protected void reset() {
		packages.clear();
	}

	@Override
	protected void run() throws DatabaseNotAvailableException {
		Status status = getStatus();
		
		Map<Integer, ClassReport> classes = Collections.newMap();
		//Map<ClassReport, PackageReport> classMap = Collections.newMap(); 
		
		status.stage = "Loading Class List (1/3)";
		status.limit = getDB().getNumClasses(getRun());
		status.progress = 0;
		
		for (ClassRecord cr: getDB().getClasses(getRun())) {
			PackageReport pr = packages.get(cr.getPackage());
			if (pr == null) {
				pr = ProblemReport.create(cr.getPackage());
				packages.put(cr.getPackage(), pr);
			}
			ClassReport report = ProblemReport.create(cr);
			report.classes = 1;
			report.flags = cr.flags;
			classes.put(cr.id, report);
			//classMap.put(report, pr);
			pr.addChild(report);
			status.progress++;
		}
		
		status.stage = "Processing Class Events (2/3)";
		status.progress = 0;
		status.limit = getDB().getNumLogs(getRun(), LogRecord.CLASS_WEAVE | LogRecord.CLASS_INITIALIZED, 0);
		for (LogRecord lr: getDB().getLogs(getRun(), 0, status.limit,
				LogRecord.CLASS_WEAVE | LogRecord.CLASS_INITIALIZED, 0)) {
			
			ClassReport cr = classes.get(lr.cnum);
			if (cr == null) {
				System.err.println("unknown class: " + lr.cnum);
			}
			
			switch (lr.event) {
			case LogRecord.CLASS_WEAVE:
				cr.weave = 1;
				break;
			case LogRecord.CLASS_INITIALIZED:
				cr.init = 1;
				break;
			}
			
			status.progress++;
		}
		
		status.stage = "Processing Instances Events (3/3)";
		status.progress = 0;
		status.limit = getDB().getNumLogs(getRun(), LogRecord.OBJECT_ALLOCATED | LogRecord.OBJECT_TAGGED, 0);
		for (LogRecord lr: getDB().getLogs(getRun(), 0, status.limit,
			LogRecord.OBJECT_ALLOCATED | LogRecord.OBJECT_TAGGED, 0)) {
			
			ClassReport cr = classes.get(lr.cnum);
			if (cr == null) {
				System.err.println("class not found: " + lr.cnum);
			}
			else {
				cr.instances = 1;
			}
			status.progress++;
		}
		
		/*for (ClassReport cr: classes.values()) {
			classMap.get(cr).addChild(cr);
		}*/
 	}
	
	public ArrayList<PackageEntry> getPackageEntries() {
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
		if (pr == null) return result;
		
		for (ClassReport cr: pr.getChildren()) {
			result.add(cr.toEntry());
		}
		
		return result;
	}

	@Override
	public ArrayList<? extends Entry> visitClassEntry(ClassEntry entry) {
		return Collections.newList(); // nothing to do;
	}

	@Override
	public ArrayList<? extends Entry> visitInstanceEntry(InstanceEntry entry) {
		return Collections.newList(); // nothing to do;
	}
	
	private static ReportFactory factory = new ReportFactory() {
		private Report report;
		{
			report = new Report("Problems", 6);
			report.headings[0] = "Package";
			report.types[0] = Report.Type.NAME;
			report.headings[1] = "Class";
			report.types[1] = Report.Type.NAME;
			report.headings[2] = "Weave";
			report.types[2] = Report.Type.COUNT;
			report.headings[3] = "Init";
			report.types[3] = Report.Type.COUNT;
			report.headings[4] = "Instances";
			report.types[4] = Report.Type.COUNT;
			report.headings[5] = "Flags";
			report.types[5] = Report.Type.FLAG;
		}
		@Override
		public ReportGenerator createGenerator(Database db, ProfilerRun run) {
			return new ProblemReportGenerator(run, db);
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
