/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Entry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.State;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.Database;
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.data.ProfilerRun;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public abstract class ReportGenerator {
	
	private static ArrayList<Report> reportList;
	private static Map<Report, ReportFactory> reports;
	
	static {
		Set<ReportFactory> factories = Collections.newSet();
		factories.add(InstancesReportGenerator.getFactory());
		factories.add(ClassesReportGenerator.getFactory());
		factories.add(WritesReportGenerator.getFactory());
		factories.add(EqualsReportGenerator.getFactory());
		factories.add(ProblemReportGenerator.getFactory());
		
		reportList = Collections.newList();
		reports = Collections.newMap();
		for (ReportFactory f: factories) {
			reportList.add(f.getReport());
			reports.put(f.getReport(), f);
		}
		
		Collections.sort(reportList);
	}

	public static ArrayList<Report> getReports() {
		return reportList;
	}

	public static ReportGenerator create(Report report, Database db, Context run) {
		if (reports.containsKey(report)) {
			return reports.get(report).createGenerator(db, run);
		}
		return null;
	}
	
	private final Context context;
	private final Status status;
	
	private Database database;
	
	protected ReportGenerator(Context context, Database database) {
		this.context = context;
		this.database = database;
		this.status = new Status();
		
		status.state = State.UNINITIALIZED;
		status.progress = 0;
	}

	protected abstract void reset();
	
	public Status generate() {
		status.state = Report.State.GENERATING;
		status.progress = 0;
		status.progress = 0;
		status.stage = "Generating";
		reset();
		new Thread() {
			public void run() {
				try {
					ReportGenerator.this.run();
					status.progress = 0;
					status.limit = 0;
					status.state = Report.State.READY;
					status.stage = "Done";
				} catch (DatabaseNotAvailableException e) {
					status.progress = 0;
					status.limit = 0;
					status.state = Report.State.UNINITIALIZED;
					status.stage = "Cancelled";
				}
			}
		}.start();
		return status;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public abstract ArrayList<? extends Entry> getReportData(Entry parent);
	protected abstract void run() throws DatabaseNotAvailableException;
	
	protected Database getDB() throws DatabaseNotAvailableException {
		if (database == null) throw new DatabaseNotAvailableException();
		return database;
	}
	
	protected Context getContext() {
		return context;
	}
	
	protected ProfilerRun getRun() {
		return context.getRun();
	}
	
	public void dispose() {
		database = null;
	}
	
	@SuppressWarnings("serial")
	public class DatabaseNotAvailableException extends Exception {}
}
