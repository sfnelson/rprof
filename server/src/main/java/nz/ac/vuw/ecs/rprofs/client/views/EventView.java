package nz.ac.vuw.ecs.rprofs.client.views;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.AsyncDataProvider;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.InstanceIdProxy;

import java.util.List;

public interface EventView extends IsWidget {

	public void setPresenter(Presenter presenter);

	public void setAvailable(int events);

	public void setFirst(int index);

	public void setFilter(int filter);

	public void setThreads(List<? extends InstanceIdProxy> threads);

	public interface Presenter {

		AsyncDataProvider<EventProxy> getDataProvider();

		void getAvailable();

		void toggleFilter(int filter);

		void clearFilter();
	}
}
