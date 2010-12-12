/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RunData extends RunInfo implements IsSerializable {

	private String program;
	private Date started;
	private Date stopped;
	private String handle;

	public RunData() {}
	public RunData(String program, Date started, Date stopped, String handle) {
		this.program = program;
		this.started = started;
		this.stopped = stopped;
		this.handle = handle;
	}

	@Override
	public String getHandle() {
		return handle;
	}

	@Override
	public String getProgram() {
		return program;
	}

	@Override
	public Date getStarted() {
		return started;
	}

	@Override
	public Date getStopped() {
		return stopped;
	}
}