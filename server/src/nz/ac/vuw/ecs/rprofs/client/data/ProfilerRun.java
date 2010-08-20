/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;
import java.util.Date;

public class ProfilerRun implements Serializable {
	private static final long serialVersionUID = -6622668634504415045L;
	public String program;
	public Date started;
	public Date stopped;
	public String handle;
	public int numClasses;
	
	public ProfilerRun() {}
	public ProfilerRun(String program, Date started, Date stopped, String handle, int numClasses) {
		this.program = program;
		this.started = started;
		this.stopped = stopped;
		this.handle = handle;
		this.numClasses = numClasses;
	}
	
	public ProfilerRun toRPC() {
		return new ProfilerRun(program, started, stopped, handle, numClasses);
	}
	
	public boolean equals(Object o) {
		if (!this.getClass().equals(o.getClass())) return false;
		ProfilerRun r = (ProfilerRun) o;
		return r.handle.equals(handle);
	}
}