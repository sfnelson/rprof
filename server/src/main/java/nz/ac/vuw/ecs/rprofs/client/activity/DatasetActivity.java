package nz.ac.vuw.ecs.rprofs.client.activity;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.DatasetPlace;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetSelectorView;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class DatasetActivity extends ListActivity<DatasetProxy> implements DatasetView.Presenter {

	private final ProfilerFactory factory;
	private final DatasetPlace place;
	private final Timer timer;

	private DatasetSelectorView view;

	public DatasetActivity(ProfilerFactory factory, DatasetPlace place) {
		this.factory = factory;
		this.place = place;
		this.timer = new Timer() {
			@Override
			public void run() {
				refresh();
			}
		};
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(this.getClass() == obj.getClass())) return false;
		return place.equals(((DatasetActivity) obj).place);
	}

	@Override
	public int hashCode() {
		return place.hashCode();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = factory.getDatasetView();

		panel.setWidget(view);

		refresh();
		timer.scheduleRepeating(10000);
	}

	@Override
	public void select(DatasetProxy dataset) {
		DatasetPlace newPlace = new DatasetPlace(dataset);
		factory.getPlaceController().goTo(newPlace);
	}

	@Override
	public void stop(DatasetProxy dataset) {
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.stopDataset(dataset).fire(new Receiver<Void>() {
			@Override
			public void onSuccess(Void response) {
				refresh();
			}
		});
	}

	@Override
	public void delete(DatasetProxy dataset) {
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.deleteDataset(dataset).fire(new Receiver<Void>() {
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
	public String mayStop() {
		return null;
	}

	@Override
	public void onCancel() {
		onStop();
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
		if (place.getDataset() != null && place.getDataset().equals(e)) {
			view.selectDataset(e);
		}
	}

	@Override
	protected void updateEntry(DatasetProxy e) {
		view.updateDataset(e);
	}

	@Override
	protected void removeEntry(DatasetProxy e) {
		view.removeDataset(e);
	}

}
