package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;

import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.ExtendedInstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.client.data.Report;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface InspectorServiceAsync {
	void getProfilerRuns(AsyncCallback<ArrayList<ProfilerRun>> callback);
	void stopProfilerRun(ProfilerRun run, AsyncCallback<ArrayList<ProfilerRun>> callback);
	void dropProfilerRun(ProfilerRun run, AsyncCallback<ArrayList<ProfilerRun>> callback);

	void getNumLogs(ProfilerRun run, int flags, int cls, AsyncCallback<Integer> callback);
	void getLogs(ProfilerRun run, int flags, int cls, int offset, int limit, AsyncCallback<ArrayList<LogData>> callback);
	void getClasses(ProfilerRun run, AsyncCallback<ArrayList<ClassData>> callback);

	void getReports(AsyncCallback<ArrayList<Report>> asyncCallback);
	void getReportStatus(Report report, ProfilerRun run, AsyncCallback<Report.Status> asyncCallback);
	void generateReport(Report report, ProfilerRun run, AsyncCallback<Report.Status> asyncCallback);
	void getReportData(Report report, ProfilerRun run, Report.Entry key, AsyncCallback<Integer> asyncCallback);
	void getReportData(Report report, ProfilerRun run, Report.Entry key, int offset, int limit, AsyncCallback<ArrayList<? extends Report.Entry>> asyncCallback);

	void getInstanceInformation(ProfilerRun run, long id, AsyncCallback<ExtendedInstanceData> instanceInformationCallback);
}
