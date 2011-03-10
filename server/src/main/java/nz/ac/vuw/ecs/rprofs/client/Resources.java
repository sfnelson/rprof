package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.ui.EventStyle;

import com.google.gwt.resources.client.ClientBundle;

public interface Resources extends ClientBundle {

	@Source("EventStyle.css")
	EventStyle eventStyle();
}
