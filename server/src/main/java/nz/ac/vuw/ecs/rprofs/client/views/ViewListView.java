package nz.ac.vuw.ecs.rprofs.client.views;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Provider;

public interface ViewListView extends IsWidget, AcceptsOneWidget {

	void setPresenter(Presenter view);

	void addPlace(String report, Provider<String> url);

	void setSelected(String report);

	interface Presenter {
		void selectView(String view);
	}

}
