package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface InspectorServiceAsync {
	void getProfilerRuns(AsyncCallback<List<ProfilerRun>> callback);
	void getClasses(ProfilerRun run, AsyncCallback<List<ClassRecord<MethodRecord>>> callback);
	void dropProfilerRun(ProfilerRun run, AsyncCallback<Void> callback);
	void getLogs(ProfilerRun run, AsyncCallback<List<LogRecord>> callback);
	void getLogs(ProfilerRun run, int offset, int limit, AsyncCallback<List<LogRecord>> callback);
	void refreshLogs(ProfilerRun currentRun, AsyncCallback<Integer> asyncCallback);
}
