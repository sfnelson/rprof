package nz.ac.vuw.ecs.rprofs.client.views;

import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.AsyncDataProvider;

public interface EventView extends IsWidget {

	public void setPresenter(Presenter presenter);
	public void setAvailable(int events);
	public void setFirst(int index);
	public void setFilter(int filter);

	public interface Presenter {

		AsyncDataProvider<EventProxy> getDataProvider();

		void getAvailable();

		void toggleFilter(int filter);
		void clearFilter();
	}
}
