package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;

import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.ExtendedInstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.InstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.RunData;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("inspector")
public interface InspectorService extends RemoteService {
	ArrayList<RunData> getProfilerRuns();
	ArrayList<RunData> stopProfilerRun(RunData run);
	ArrayList<RunData> dropProfilerRun(RunData run);

	int getNumLogs(RunData currentRun, int flags, ClassData cls);
	ArrayList<LogData> getLogs(RunData run, int flags, ClassData cls, int offset, int limit);
	ArrayList<ClassData> getClasses(RunData run);

	ArrayList<Report> getReports();
	Report.Status getReportStatus(Report report, RunData run);
	Report.Status generateReport(Report report, RunData run);
	Integer getReportData(Report report, RunData run, Report.Entry key);
	ArrayList<? extends Report.Entry> getReportData(Report report, RunData run, Report.Entry key, int offset, int limit);

	ExtendedInstanceData getInstanceInformation(RunData run, InstanceData id);
}
