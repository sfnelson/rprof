package nz.ac.vuw.ecs.rprofs.client.views;

import com.google.gwt.user.client.ui.IsWidget;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;

public interface DatasetView extends IsWidget {

	void setPresenter(Presenter presenter);

	void setDataset(DatasetProxy dataset);

	DatasetProxy getDataset();

	public interface Presenter {
		void select(DatasetProxy dataset);

		void stop(DatasetProxy dataset);

		void delete(DatasetProxy dataset);
	}

}
