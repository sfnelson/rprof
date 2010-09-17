package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

public interface ProfilerRunListener {

	public void profilerRunsAvailable(List<ProfilerRun> result);
	
}
