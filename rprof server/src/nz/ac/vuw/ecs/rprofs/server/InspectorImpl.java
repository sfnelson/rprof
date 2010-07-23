package nz.ac.vuw.ecs.rprofs.server;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Inspector;
import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class InspectorImpl extends RemoteServiceServlet implements Inspector {

	public List<ProfilerRun> getProfilerRuns() {
		return Context.getInstance().db().getProfiles();
	}
	
	public void dropProfilerRun(ProfilerRun run) {
		Context.getInstance().db().dropRun(run);
	}
	
	public List<ClassRecord> getClasses(ProfilerRun run) {
		return Context.getInstance().db().getClasses(run);
	}

	public List<LogRecord> getLogs(ProfilerRun run) {
		return Context.getInstance().db().getLogs(run);
	}
	
	public List<LogRecord> getLogs(ProfilerRun run, int offset, int limit) {
		return Context.getInstance().db().getLogs(run, offset, limit);
	}

	public int refreshLogs(ProfilerRun run) {
		return Context.getInstance().db().getNumLogs(run);
	}

}
