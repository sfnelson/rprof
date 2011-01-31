package nz.ac.vuw.ecs.rprofs.client.views;

import nz.ac.vuw.ecs.rprofs.client.requests.EventProxy;

import com.google.gwt.user.client.ui.IsWidget;

public interface EventView extends IsWidget {

	void setPresenter(Presenter presenter);
	void setTitle(String title);
	void addEvent(EventProxy proxy);
	void clear();

	interface Presenter {

	}
}
