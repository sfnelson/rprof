package nz.ac.vuw.ecs.rprofs.server;

import java.util.ArrayList;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.InspectorService;
import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.InstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.data.Context.ActiveContext;
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
	public ArrayList<ProfilerRun> dropProfilerRun(ProfilerRun run) {
		Context.dropRun(run);
		
		return getProfilerRuns();
	}
	
	@Override
	public ArrayList<ProfilerRun> stopProfilerRun(ProfilerRun run) {
		ActiveContext current = Context.getCurrent();
		if (current != null) {
			current.stop();
		}
		
		return getProfilerRuns();
	}
	
	@Override
	public ArrayList<ClassData> getClasses(ProfilerRun run) {
		ArrayList<ClassData> cl = Collections.newList();
		for (ClassRecord cr: Context.getInstance(run).getClasses()) {
			cl.add(cr.toRPC());
		}
		return cl;
	}

	@Override
	public ArrayList<LogData> getLogs(ProfilerRun run, int type, int cls,
			int offset, int limit) {
		ArrayList<LogData> records = Collections.newList();
		for (LogInfo r: Context.getInstance(run).getLogs(offset, limit, type, cls)) {
			records.add(r.toRPC());
		}
		System.out.println("returning events " + offset + " to " + (offset + limit));
		return records;
	}

	@Override
	public int getNumLogs(ProfilerRun run, int type, int cls) {
		int result = Context.getInstance(run).getNumLogs(type, cls);
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

	@Override
	public InstanceData getInstanceInformation(ProfilerRun run, long id) {
		return Context.getInstance(run).getInstanceInformation(id);
	}

}
