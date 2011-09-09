package nz.ac.vuw.ecs.rprofs.client.views;

import com.google.gwt.user.client.ui.IsWidget;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;

import java.util.List;


public interface DatasetListView extends IsWidget {

	void setPresenter(Presenter presenter);

	void setNumDatasets(int numDatasets);

	void setDatasets(List<DatasetProxy> datasets);

	void setSelected(DatasetProxy dataset);

	interface Presenter {
		void selectDataset(DatasetProxy dataset);

		void deleteDataset(DatasetProxy dataset);

		void stopDataset(DatasetProxy dataset);
	}

}
