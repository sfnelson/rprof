/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.ArrayList;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Entry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.State;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.Database;
import nz.ac.vuw.ecs.rprofs.server.data.ProfilerRun;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public abstract class ReportGenerator {
	
	protected interface ReportGeneratorFactory {
		public ReportGenerator create(ProfilerRun run, Database db);
	}
	
	private static Map<Report, ReportGeneratorFactory> reports = Collections.newMap();
	
	static {
		reports.put(InstanceReportGenerator.getReport(), InstanceReportGenerator.getFactory());
	}

	public static ArrayList<Report> getReports() {
		ArrayList<Report> list = Collections.newList();
		list.addAll(reports.keySet());
		Collections.sort(list);
		return list;
	}

	public static ReportGenerator create(Report report, ProfilerRun run, Database db) {
		if (reports.containsKey(report)) {
			return reports.get(report).create(run, db);
		}
		return null;
	}
	
	private final ProfilerRun run;
	private final Status status;
	
	private Database database;
	
	protected ReportGenerator(ProfilerRun run, Database database) {
		this.run = run;
		this.database = database;
		this.status = new Status();
		
		status.state = State.UNINITIALIZED;
		status.progress = 0;
	}

	public Status generate() {
		status.state = Report.State.GENERATING;
		status.progress = 0;
		new Thread() {
			public void run() {
				ReportGenerator.this.run();
				status.progress = 100;
				status.state = Report.State.READY;
				status.stage = "Generating";
			}
		}.start();
		return status;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public abstract ArrayList<? extends Entry> getReportData(Entry parent);
	protected abstract void run();
	
	protected Database getDB() throws DatabaseNotAvailableException {
		if (database == null) throw new DatabaseNotAvailableException();
		return database;
	}
	
	protected ProfilerRun getRun() {
		return run;
	}
	
	public void dispose() {
		database = null;
	}
	
	@SuppressWarnings("serial")
	public class DatabaseNotAvailableException extends Exception {}
}
