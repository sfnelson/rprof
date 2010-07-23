package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("inspector")
public interface Inspector extends RemoteService {
	List<ProfilerRun> getProfilerRuns();
	List<ClassRecord> getClasses(ProfilerRun run);
	List<LogRecord> getLogs(ProfilerRun run);
	List<LogRecord> getLogs(ProfilerRun run, int offset, int limit);
	void dropProfilerRun(ProfilerRun run);
	int refreshLogs(ProfilerRun currentRun);
}
