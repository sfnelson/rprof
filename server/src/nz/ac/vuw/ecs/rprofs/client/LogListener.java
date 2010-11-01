package nz.ac.vuw.ecs.rprofs.client;

import java.util.Collection;

import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;

public interface LogListener {

	public void logsAvailable(int type, int cls, int available, LogCallback callback);
	public void logsAvailable(int type, int cls, int offset, int limit, Collection<LogRecord> result, LogCallback callback);
	
	interface LogCallback {
		void doRequest(int offset, int limit);
	}
}
