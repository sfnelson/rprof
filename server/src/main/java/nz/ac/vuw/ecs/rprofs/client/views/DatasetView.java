package nz.ac.vuw.ecs.rprofs.client.views;

import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;

import com.google.gwt.user.client.ui.IsWidget;

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
