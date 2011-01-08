package nz.ac.vuw.ecs.rprofs.client.events;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;

import com.google.gwt.event.shared.EventHandler;

public interface ProfilerRunHandler extends EventHandler {

	public void profilerRunsAvailable(List<DatasetProxy> result);

}
