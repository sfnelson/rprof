package nz.ac.vuw.ecs.rprofs.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.requestfactory.shared.Receiver;
import nz.ac.vuw.ecs.rprofs.client.place.ShowDataset;
import nz.ac.vuw.ecs.rprofs.client.place.shared.HasDataset;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;

import javax.annotation.Nullable;
import java.util.List;

public class SelectDatasetActivity extends AbstractActivity implements DatasetListView.Presenter {

	private final DatasetListView view;
	private final PlaceController pc;
	private final Provider<DatasetRequest> rf;

	@Nullable
	private HasDataset place;

	@Inject
	public SelectDatasetActivity(DatasetListView view, PlaceController pc, Provider<DatasetRequest> rf) {
		this.view = view;
		this.pc = pc;
		this.rf = rf;
	}

	public SelectDatasetActivity setPlace(@Nullable HasDataset place) {
		this.place = place;
		return this;
	}

	public void setSelected(@Nullable HasDataset place) {
		this.place = place;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view.setPresenter(this);

		panel.setWidget(view);

		refresh();
	}

	@Override
	public void selectDataset(DatasetProxy dataset) {
		ShowDataset newPlace = new ShowDataset(dataset.getHandle());
		pc.goTo(newPlace);
	}

	@Override
	public void stopDataset(DatasetProxy dataset) {
		rf.get().stopDataset(dataset.getHandle()).fire(new Receiver<Void>() {
			@Override
			public void onSuccess(Void response) {
				refresh();
			}
		});
	}

	@Override
	public void deleteDataset(DatasetProxy dataset) {
		rf.get().deleteDataset(dataset.getHandle()).fire(new Receiver<Void>() {
			@Override
			public void onSuccess(Void response) {
				refresh();
			}
		});
	}

	private void refresh() {
		rf.get().findAllDatasets().fire(new Receiver<List<DatasetProxy>>() {
			@Override
			public void onSuccess(List<DatasetProxy> response) {
				refresh(response);
			}
		});
	}

	private void refresh(List<DatasetProxy> datasets) {
		view.setNumDatasets(datasets.size());
		view.setDatasets(datasets);

		HasDataset place = this.place;
		if (place != null) {
			for (DatasetProxy d : datasets) {
				if (place.getDatasetHandle().equals(d.getHandle())) {
					view.setSelected(d);
				}
			}
		}
	}
}
