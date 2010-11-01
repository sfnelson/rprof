package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;
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
	ArrayList<ClassRecord<MethodRecord, FieldRecord>> getClasses(ProfilerRun run);
	int getNumLogs(ProfilerRun currentRun, int flags, int cls);
	ArrayList<LogRecord> getLogs(ProfilerRun run, int flags, int cls, int offset, int limit);
	void stopProfilerRun(ProfilerRun run);
	void dropProfilerRun(ProfilerRun run);
	
	ArrayList<Report> getReports();
	Report.Status getReportStatus(Report report, ProfilerRun run);
	Report.Status generateReport(Report report, ProfilerRun run);
	Integer getReportData(Report report, ProfilerRun run, Report.Entry key);
	ArrayList<? extends Report.Entry> getReportData(Report report, ProfilerRun run, Report.Entry key, int offset, int limit);
}
