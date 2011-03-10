package nz.ac.vuw.ecs.rprofs.client.views;

import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.AsyncDataProvider;

public interface EventReportView extends IsWidget {

	public void setPresenter(Presenter presenter);

	public interface Presenter {

		AsyncDataProvider<EventProxy> getDataProvider();

	}
}
