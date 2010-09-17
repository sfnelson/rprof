package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.client.data.Report;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface InspectorServiceAsync {
	void getProfilerRuns(AsyncCallback<ArrayList<ProfilerRun>> callback);
	void getClasses(ProfilerRun run, AsyncCallback<ArrayList<ClassRecord<MethodRecord, FieldRecord>>> callback);
	void dropProfilerRun(ProfilerRun run, AsyncCallback<Void> callback);
	void getNumLogs(ProfilerRun run, int flags, AsyncCallback<Integer> callback);
	void getLogs(ProfilerRun run, int flags, int offset, int limit, AsyncCallback<ArrayList<LogRecord>> callback);

	void getReports(AsyncCallback<ArrayList<Report>> asyncCallback);
	void getReportStatus(Report report, ProfilerRun run, AsyncCallback<Report.Status> asyncCallback);
	void generateReport(Report report, ProfilerRun run, AsyncCallback<Report.Status> asyncCallback);
	void getReportData(Report report, ProfilerRun run, Report.Entry key, AsyncCallback<Integer> asyncCallback);
	void getReportData(Report report, ProfilerRun run, Report.Entry key, int offset, int limit, AsyncCallback<ArrayList<? extends Report.Entry>> asyncCallback);
}
