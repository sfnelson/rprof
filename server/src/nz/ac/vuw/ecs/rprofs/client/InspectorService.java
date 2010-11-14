package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;

import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.ExtendedInstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.client.data.Report;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("inspector")
public interface InspectorService extends RemoteService {
	ArrayList<ProfilerRun> getProfilerRuns();
	ArrayList<ProfilerRun> stopProfilerRun(ProfilerRun run);
	ArrayList<ProfilerRun> dropProfilerRun(ProfilerRun run);
	
	int getNumLogs(ProfilerRun currentRun, int flags, int cls);
	ArrayList<LogData> getLogs(ProfilerRun run, int flags, int cls, int offset, int limit);
	ArrayList<ClassData> getClasses(ProfilerRun run);
	
	ArrayList<Report> getReports();
	Report.Status getReportStatus(Report report, ProfilerRun run);
	Report.Status generateReport(Report report, ProfilerRun run);
	Integer getReportData(Report report, ProfilerRun run, Report.Entry key);
	ArrayList<? extends Report.Entry> getReportData(Report report, ProfilerRun run, Report.Entry key, int offset, int limit);
	
	ExtendedInstanceData getInstanceInformation(ProfilerRun run, long id);
}
