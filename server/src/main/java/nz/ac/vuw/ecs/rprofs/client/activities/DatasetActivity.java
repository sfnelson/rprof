package nz.ac.vuw.ecs.rprofs.client.activities;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.events.DatasetEvent;
import nz.ac.vuw.ecs.rprofs.client.places.InspectorPlace;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class DatasetActivity extends ListActivity<DatasetProxy> implements DatasetView.Presenter {

	private final ProfilerFactory factory;
	private final String selected;
	private final Timer timer;

	private DatasetListView view;

	public DatasetActivity(ProfilerFactory factory, String selected) {
		this.factory = factory;
		this.selected = selected;
		this.timer = new Timer() {
			@Override
			public void run() {
				refresh();
			}
		};
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		this.view = factory.getInspectorView().getDatasetListView();

		refresh();
		timer.scheduleRepeating(10000);
	}

	@Override
	public void select(DatasetProxy dataset) {
		PlaceController pc = factory.getPlaceController();
		Place place = pc.getWhere();
		if (place instanceof InspectorPlace) {
			place = ((InspectorPlace) place).setDataset(dataset);
		}
		else {
			place = new InspectorPlace(dataset, null);
		}

		pc.goTo(place);
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
		if (selected != null && selected.equals(e.getHandle())) {
			view.selectDataset(e);

			factory.getEventBus().fireEvent(new DatasetEvent(e));
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
