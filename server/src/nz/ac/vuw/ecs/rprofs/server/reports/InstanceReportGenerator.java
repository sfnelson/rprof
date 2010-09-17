/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.InstanceRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Entry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.Database;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.server.data.ProfilerRun;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceReportGenerator extends ReportGenerator implements Report.EntryVisitor<ArrayList<? extends Report.Entry>> {

	private Map<String, InstanceReport<Void, String, ClassRecord>> packages = Collections.newMap();
	private Map<ClassRecord, InstanceReport<String, ClassRecord, InstanceRecord>> classes = Collections.newMap();
	private Map<InstanceRecord, InstanceReport<ClassRecord, InstanceRecord, Void>> instances = Collections.newMap();

	private Map<Integer, ClassRecord> classMap = Collections.newMap();
	private Map<Long, InstanceRecord> instanceMap = Collections.newMap();

	public InstanceReportGenerator(ProfilerRun run, Database database) {
		super(run, database);
	}

	public void run() {
		Status status = getStatus();		
		
		try {
			status.progress = 0;
			status.stage = "Loading Class List (1/3)";
			List<ClassRecord> classList = getDB().getClasses(getRun());
			for (int i = 0; i < classList.size(); i++) {
				ClassRecord cr = classList.get(i);
				InstanceReport<Void, String, ClassRecord> pkg = packages.get(cr.getPackage());
				if (pkg == null) {
					pkg = InstanceReport.create(null, cr.getPackage());
					packages.put(cr.getPackage(), pkg);
				}
				InstanceReport<String, ClassRecord, InstanceRecord> cls = InstanceReport.create(pkg, cr);
				cls.classes = 1;
				classes.put(cr, cls);
				classMap.put(cr.id, cr);
				status.progress = i * 100 / classList.size();
			}
			
			status.progress = 0;
			status.stage = "Processing Allocation Logs (2/3)";
			int progress = 0;
			int available = getDB().getNumLogs(getRun(), LogRecord.ALLOCATION);
			for (LogRecord lr: getDB().getLogs(getRun(), 0, available, LogRecord.ALLOCATION)) {
				ClassRecord cr = classMap.get(lr.cnum);
				if (cr == null) continue;
				InstanceReport<String, ClassRecord, InstanceRecord> cls = classes.get(cr);

				InstanceRecord ir = instanceMap.get(lr.args[0]);
				if (ir == null) {
					ir = new InstanceRecord();
					ir.cnum = lr.cnum;
					ir.mnum = lr.mnum;
					ir.id = lr.args[0];
					instanceMap.put(lr.args[0], ir);
				}

				InstanceReport<ClassRecord, InstanceRecord, Void> instance = instances.get(ir);
				if (instance == null) {
					instance = InstanceReport.create(cls, ir);
					instance.instances = 1;
					instances.put(ir, instance);
				}
				
				progress++;
				status.progress = progress * 100 / available;
			}
			
			status.progress = 0;
			status.stage = "Processing field accesses (3/3)";
			available = getDB().getNumLogs(getRun(), LogRecord.FIELDS);
			progress = 0;
			for (LogRecord lr: getDB().getLogs(getRun(), 0, available, LogRecord.FIELDS)) {
				InstanceRecord ir = instanceMap.get(lr.args[0]);
				ClassRecord cr = classMap.get(lr.cnum);
				FieldRecord fr = cr.getFields().get(lr.mnum - 1);
				if (ir == null) continue;
				if (lr.event == LogRecord.FIELD_READ) {
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

				progress++;
				status.progress = progress * 100 / available;
			}
		} catch (DatabaseNotAvailableException ex) {
			// Run was disposed while it the report was being generated, fail quietly.
		}
		
		status.progress = 100;
		status.stage = "Done";
	}

	/**
	 * @return
	 */
	public ArrayList<? extends Entry> getPackageReports() {
		ArrayList<Report.PackageEntry> result = Collections.newList();
		for (InstanceReport<?, String, ?> r: packages.values()) {
			Report.PackageEntry entry = new Report.PackageEntry(r.target, r.classes, r.instances);
			r.toEntry(entry);
			result.add(entry);
		}
		return result;
	}

	@Override
	public ArrayList<ClassEntry> visitPackageEntry(PackageEntry entry) {
		ArrayList<ClassEntry> result = Collections.newList();

		InstanceReport<Void, String, ClassRecord> pkg = packages.get(entry.pkg);
		if (pkg == null) return result;
		for (InstanceReport<String, ClassRecord, ?> r: pkg.children) {
			ClassEntry e = new ClassEntry(r.target.toRPC(), r.instances);
			r.toEntry(e);
			result.add(e);
		}
		return result;
	}


	@Override
	public ArrayList<InstanceEntry> visitClassEntry(ClassEntry entry) {
		ArrayList<InstanceEntry> result = Collections.newList();
		ClassRecord cr = classMap.get(entry.cls.id);
		if (cr == null) return result;
		InstanceReport<?, ClassRecord, InstanceRecord> cls = classes.get(cr);
		for (InstanceReport<ClassRecord, InstanceRecord, ?> r: cls.children) {
			InstanceEntry e = new InstanceEntry(r.target.id);
			r.toEntry(e);
			result.add(e);
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

	private static Report report;
	
	static {
		report = new Report("Instances");
		report.headings.add("Package");
		report.types.add(Report.Type.NAME);
		report.headings.add("Class");
		report.types.add(Report.Type.NAME);
		report.headings.add("Instance");
		report.types.add(Report.Type.OBJECT);
		report.headings.add("Reads");
		report.types.add(Report.Type.COUNT);
		report.headings.add("Writes");
		report.types.add(Report.Type.COUNT);
		report.headings.add("EReads");
		report.types.add(Report.Type.COUNT);
		report.headings.add("EWrites");
		report.types.add(Report.Type.COUNT);
	}
	
	static Report getReport() {
		return report;
	}

	static ReportGeneratorFactory getFactory() {
		return new ReportGeneratorFactory() {
			@Override
			public ReportGenerator create(ProfilerRun run, Database db) {
				return new InstanceReportGenerator(run, db);
			}
		};
	}
}
