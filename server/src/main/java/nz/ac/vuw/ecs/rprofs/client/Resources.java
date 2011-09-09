package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.resources.client.ClientBundle;
import nz.ac.vuw.ecs.rprofs.client.ui.EventStyle;

public interface Resources extends ClientBundle {

	@Source("EventStyle.css")
	EventStyle eventStyle();
}
