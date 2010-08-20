package nz.ac.vuw.ecs.rprofs.server;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.InspectorService;
import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class InspectorServiceImpl extends RemoteServiceServlet implements InspectorService {

	public List<ProfilerRun> getProfilerRuns() {
		List<ProfilerRun> profiles = Collections.newList();
		for (ProfilerRun run: Context.db().getProfiles()) {
			profiles.add(run.toRPC());
		}
		return profiles;
	}
	
	public void dropProfilerRun(ProfilerRun run) {
		Context.db().dropRun(run);
	}
	
	public List<ClassRecord<MethodRecord, FieldRecord>> getClasses(ProfilerRun run) {
		List<ClassRecord<MethodRecord, FieldRecord>> cl = Collections.newList();
		for (ClassRecord<? extends MethodRecord, ? extends FieldRecord> cr: Context.db().getClasses(run)) {
			cl.add(cr.toRPC());
		}
		return cl;
	}

	public List<LogRecord> getLogs(ProfilerRun run) {
		List<LogRecord> records = Collections.newList();
		for (LogRecord r: Context.db().getLogs(run)) {
			records.add(r.toRPC());
		}
		return records;
	}
	
	public List<LogRecord> getLogs(ProfilerRun run, int offset, int limit) {
		List<LogRecord> records = Collections.newList();
		for (LogRecord r: Context.db().getLogs(run, offset, limit)) {
			records.add(r.toRPC());
		}
		return records;
	}

	public int refreshLogs(ProfilerRun run) {
		return Context.db().getNumLogs(run);
	}

}
