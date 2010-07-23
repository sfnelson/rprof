package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;

public interface LogListener {

	public void logsChanged(int number);
	public void logsAvailable(List<LogRecord> logs, int offset);
	
}
