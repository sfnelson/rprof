/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.history;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class History {

	String handle;
	public ProfilerRun run;
	public Long id;
	public String view;

	public String toString() {
		StringBuilder r = new StringBuilder();
		boolean first = true;
		
		if (run != null) handle = run.handle;
		if (handle != null) first = add(r, "run", handle, first);
		if (id != null) first = add(r, "id", id.toString(), first);
		if (view != null) first = add(r, "view", view.toString(), first);
		
		return r.toString();
	}
	
	private boolean add(StringBuilder n, String name, String value, boolean first) {
		if (!first) {
			n.append("&");
		}
		n.append(name);
		n.append("=");
		n.append(value);
		return false;
	}
}
