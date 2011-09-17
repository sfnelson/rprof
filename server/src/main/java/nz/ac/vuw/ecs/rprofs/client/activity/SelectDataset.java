package nz.ac.vuw.ecs.rprofs.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.requestfactory.shared.EntityProxyChange;
import com.google.web.bindery.requestfactory.shared.Receiver;
import nz.ac.vuw.ecs.rprofs.client.place.HasDataset;
import nz.ac.vuw.ecs.rprofs.client.place.PlaceBuilder;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;

import javax.annotation.Nullable;
import java.util.List;

public class SelectDataset extends AbstractActivity
		implements DatasetListView.Presenter, EntityProxyChange.Handler<DatasetProxy> {

	private final DatasetListView view;
	private final PlaceController pc;
	private final Provider<DatasetRequest> rf;

	@Nullable
	private HasDataset place;

	@Inject
	public SelectDataset(DatasetListView view, PlaceController pc, Provider<DatasetRequest> rf) {
		this.view = view;
		this.pc = pc;
		this.rf = rf;
	}

	@Override
	public void onProxyChange(EntityProxyChange<DatasetProxy> event) {
		GWT.log("dataset changed, refreshing list");
		refreshDatasetList();
	}

	public SelectDataset setPlace(@Nullable HasDataset place) {
		this.place = place;
		return this;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view.setPresenter(this);

		panel.setWidget(view);

		refreshDatasetList();

		EntityProxyChange.registerForProxyType(eventBus, DatasetProxy.class, this);
	}

	@Override
	public void selectDataset(DatasetProxy dataset) {
		Place newPlace = PlaceBuilder.create()
				.setDataset(dataset.getId())
				.get(pc.getWhere());
		pc.goTo(newPlace);
	}

	@Override
	public void stopDataset(DatasetProxy dataset) {
		rf.get().stopDataset(dataset.getId()).fire(new Receiver<Void>() {
			@Override
			public void onSuccess(Void response) {
				refreshDatasetList();
			}
		});
	}

	@Override
	public void deleteDataset(DatasetProxy dataset) {
		rf.get().deleteDataset(dataset.getId()).fire(new Receiver<Void>() {
			@Override
			public void onSuccess(Void response) {
				refreshDatasetList();
			}
		});
	}

	private void refreshDatasetList() {
		rf.get().findAllDatasets().with("id")
				.fire(new Receiver<List<DatasetProxy>>() {
					@Override
					public void onSuccess(List<DatasetProxy> response) {
						showDatasets(response);
					}
				});
	}

	private void showDatasets(List<DatasetProxy> datasets) {
		view.setNumDatasets(datasets.size());
		view.setDatasets(datasets);

		HasDataset place = this.place;
		if (place != null && place.getDatasetId() != null) {
			for (DatasetProxy d : datasets) {
				if (place.getDatasetId().getValue() == d.getId().getValue()) {
					view.setSelected(d);
				}
			}
		}
	}
}
