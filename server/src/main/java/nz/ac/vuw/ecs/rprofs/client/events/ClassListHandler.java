/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.events;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ClassData;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface ClassListHandler extends EventHandler {

	public void onClassListAvailable(List<ClassData> classes);

}
