package nz.ac.vuw.ecs.rprofs.client.views;

import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetView.Presenter;

import com.google.gwt.user.client.ui.IsWidget;


public interface DatasetSelectorView extends IsWidget {

	public void addDataset(DatasetProxy dataset, Presenter presenter);
	public void updateDataset(DatasetProxy dataset);
	public void selectDataset(DatasetProxy dataset);
	public void removeDataset(DatasetProxy dataset);

}
