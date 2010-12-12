package nz.ac.vuw.ecs.rprofs.client;

import java.util.Collection;

import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;

public interface LogListener {

	public void logsAvailable(int type, ClassData cls, int available, LogCallback callback);
	public void logsAvailable(int type, ClassData cls, int offset, int limit, Collection<LogData> result, LogCallback callback);

	interface LogCallback {
		void doRequest(int offset, int limit);
	}
}
