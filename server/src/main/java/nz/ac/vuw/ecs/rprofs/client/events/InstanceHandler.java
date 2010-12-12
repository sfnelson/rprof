/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface InstanceHandler extends EventHandler {
	public void onInstanceEvent(InstanceEvent event);
}
