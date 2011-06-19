package nz.ac.vuw.ecs.rprofs.client.activity;

import nz.ac.vuw.ecs.rprofs.client.Factory;
import nz.ac.vuw.ecs.rprofs.client.activity.shared.AbstractListActivity;
import nz.ac.vuw.ecs.rprofs.client.place.ShowDataset;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class SelectDatasetActivity extends AbstractListActivity<DatasetProxy> implements DatasetView.Presenter {

	private final Factory factory;
	private final Timer timer;

	private String selected;
	private DatasetListView view;

	public SelectDatasetActivity(Factory factory) {
		this.factory = factory;
		this.timer = new Timer() {
			@Override
			public void run() {
				refresh();
			}
		};
	}

	public void setSelected(String datasetHandle) {
		if (selected != null) {
			if (selected.equals(datasetHandle)) {
				return;
			}
			else {
				view.selectDataset(null);
			}
		}

		this.selected = datasetHandle;

		refresh();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = factory.getDatasetView();

		panel.setWidget(view);

		timer.scheduleRepeating(10000);
		refresh();
	}

	@Override
	public void select(DatasetProxy dataset) {
		ShowDataset newPlace = new ShowDataset(dataset.getHandle());
		factory.getPlaceController().goTo(newPlace);
	}

	@Override
	public void stop(DatasetProxy dataset) {
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.stopDataset(dataset.getHandle()).fire(new Receiver<Void>() {
			@Override
			public void onSuccess(Void response) {
				refresh();
			}
		});
	}

	@Override
	public void delete(DatasetProxy dataset) {
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.deleteDataset(dataset.getHandle()).fire(new Receiver<Void>() {
			@Override
			public void onSuccess(Void response) {
				refresh();
			}
		});
	}

	private void refresh() {
		refresh(factory.getRequestFactory().datasetRequest().findAllDatasets());
	}

	@Override
	public void onCancel() {
		timer.cancel();
	}

	@Override
	public void onStop() {
		timer.cancel();

		super.onStop();

		this.view = null;
	}

	@Override
	protected void addEntry(DatasetProxy e) {
		view.addDataset(e, this);

		if (e.getHandle().equals(selected)) {
			view.selectDataset(e);
		}
	}

	@Override
	protected void updateEntry(DatasetProxy e) {
		view.updateDataset(e);

		if (e.getHandle().equals(selected)) {
			view.selectDataset(e);
		}
	}

	@Override
	protected void removeEntry(DatasetProxy e) {
		view.removeDataset(e);
	}

}
