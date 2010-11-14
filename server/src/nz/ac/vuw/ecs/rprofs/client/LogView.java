/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.history.History;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class LogView implements View {

	@Override
	public Widget createWidget(History history) {
		return new LogPanel();
	}

	@Override
	public String getDescription() {
		return "Show log records for the selected profiler run";
	}

	@Override
	public String getIdentifier() {
		return "events";
	}

	@Override
	public String getTitle() {
		return "Events";
	}

}
