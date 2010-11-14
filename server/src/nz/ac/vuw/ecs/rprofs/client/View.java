package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.history.History;

import com.google.gwt.user.client.ui.Widget;

public interface View {
	String getIdentifier();
	String getTitle();
	String getDescription();
	Widget createWidget(History history);
}
