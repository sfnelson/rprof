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
}