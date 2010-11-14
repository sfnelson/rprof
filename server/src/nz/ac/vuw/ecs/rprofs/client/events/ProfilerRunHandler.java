package nz.ac.vuw.ecs.rprofs.client.events;

import java.util.List;

import com.google.gwt.event.shared.EventHandler;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

public interface ProfilerRunHandler extends EventHandler {

	public void profilerRunsAvailable(List<ProfilerRun> result);
	
}
