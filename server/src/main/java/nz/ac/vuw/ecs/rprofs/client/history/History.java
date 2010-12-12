/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.history;

import nz.ac.vuw.ecs.rprofs.client.data.InstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.RunData;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class History {

	String handle;
	public RunData run;
	public InstanceData id;
	public String view;

	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		boolean first = true;

		if (run != null) handle = run.getHandle();
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
