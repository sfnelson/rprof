package nz.ac.vuw.ecs.rprofs.server;

import java.util.ArrayList;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.InspectorService;
import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.Context.ActiveContext;
import nz.ac.vuw.ecs.rprofs.server.reports.ReportGenerator;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class InspectorServiceImpl extends RemoteServiceServlet implements InspectorService {

	@Override
	public ArrayList<ProfilerRun> getProfilerRuns() {
		ArrayList<ProfilerRun> profiles = Collections.newList();
		for (ProfilerRun run: Context.getRuns()) {
			profiles.add(run.toRPC());
		}
		return profiles;
	}
	
	@Override
	public void dropProfilerRun(ProfilerRun run) {
		Context.dropRun(run);
	}
	
	@Override
	public void stopProfilerRun(ProfilerRun run) {
		ActiveContext current = Context.getCurrent();
		if (current != null) {
			current.stop();
		}
	}
	
	@Override
	public ArrayList<ClassRecord<MethodRecord, FieldRecord>> getClasses(ProfilerRun run) {
		ArrayList<ClassRecord<MethodRecord, FieldRecord>> cl = Collections.newList();
		for (ClassRecord<? extends MethodRecord, ? extends FieldRecord> cr: Context.getInstance(run).getClasses()) {
			cl.add(cr.toRPC());
		}
		return cl;
	}

	@Override
	public ArrayList<LogRecord> getLogs(ProfilerRun run, int type, int cls,
			int offset, int limit) {
		ArrayList<LogRecord> records = Collections.newList();
		for (LogRecord r: Context.getInstance(run).getLogs(run, offset, limit, type, cls)) {
			records.add(r.toRPC());
		}
		System.out.println("returning events " + offset + " to " + (offset + limit));
		return records;
	}

	@Override
	public int getNumLogs(ProfilerRun run, int type, int cls) {
		int result = Context.getInstance(run).getNumLogs(run, type, cls);
		System.out.println(result + " events available");
		return result;
	}

	@Override
	public Status generateReport(Report report, ProfilerRun run) {
		return Context.getInstance(run).getReport(report).generate();
	}

	@Override
	public Integer getReportData(Report report, ProfilerRun run, Report.Entry key) {
		List<? extends Report.Entry> data = Context.getInstance(run).getReport(report).getReportData(key);
		
		return data.size();
	}
	
	@Override
	public ArrayList<? extends Report.Entry> getReportData(Report report, ProfilerRun run, Report.Entry key, int offset, int limit) {
		ArrayList<? extends Report.Entry> data = Context.getInstance(run).getReport(report).getReportData(key);
		Collections.sort(data);
		
		if (offset == 0 && data.size() <= limit) {
			return data;
		}
		
		ArrayList<Report.Entry> result = Collections.newList();
		for (int i = offset; i < data.size(); i++) {
			if (i - offset >= limit) break;
			result.add(data.get(i));
		}
		
		return result;
	}

	@Override
	public Status getReportStatus(Report report, ProfilerRun run) {
		return Context.getInstance(run).getReport(report).getStatus();
	}

	@Override
	public ArrayList<Report> getReports() {
		return ReportGenerator.getReports();
	}

}
