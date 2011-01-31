package nz.ac.vuw.ecs.rprofs.client.activity;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.DatasetPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InspectorPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InstancePlace;
import nz.ac.vuw.ecs.rprofs.client.place.ReportPlace;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlaceHistoryMapper;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.requests.InstanceProxy;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class InspectorActivity extends AbstractActivity {

	private final ProfilerFactory factory;
	private final InspectorPlace place;

	private boolean started = false;
	private DatasetPlace datasetPlace;
	private InstancePlace instancePlace;

	public InspectorActivity(ProfilerFactory factory, InspectorPlace place) {
		this.factory = factory;
		this.place = place;

		if (place.getDatasetHandle() != null) {
			DatasetRequest rq = factory.getRequestFactory().datasetRequest();
			rq.findDataset(place.getDatasetHandle()).fire(new Receiver<DatasetProxy>() {
				@Override
				public void onSuccess(DatasetProxy dataset) {
					DatasetPlace place;
					if (dataset == null) {
						place = DatasetPlace.NO_DATASET;
					}
					else {
						place = new DatasetPlace(dataset);
					}

					setDataset(place);
				}
			});
		}
		else {
			datasetPlace = DatasetPlace.NO_DATASET;
		}
	}

	private void setDataset(final DatasetPlace dataset) {
		datasetPlace = dataset;

		if (place.getInstance() != 0 && dataset.getDataset() != null) {
			DatasetRequest rq = factory.getRequestFactory().datasetRequest();
			rq.findInstance(place.getInstance()).using(dataset.getDataset())
			.with("type", "constructor", "events")
			.fire(new Receiver<InstanceProxy>() {

				@Override
				public void onSuccess(InstanceProxy response) {
					InstancePlace place;
					if (response == null) {
						place = InstancePlace.NO_INSTANCE;
					}
					else {
						place = new InstancePlace(response, dataset);
					}

					setInstance(place);
				}

			});
		}

		// by now, the activity may have been started
		datasetAvailable();
	}

	private void setInstance(InstancePlace instance) {
		instancePlace = instance;

		// by now, the activity may have been started
		instanceAvailable();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(factory.getInspectorView());

		started = true;

		datasetAvailable();
	}

	private void datasetAvailable() {
		if (!started || datasetPlace == null) return;

		factory.getEventBus().fireEvent(new PlaceChangeEvent(datasetPlace));

		ReportPlace<?> reportPlace;
		if (place.getReportHandle() == null) {
			reportPlace = ReportPlace.NO_REPORT;
		}
		else {
			reportPlace = ReportPlaceHistoryMapper.instance().getPlace(place.getReportHandle());
		}

		if (datasetPlace != null) {
			reportPlace = reportPlace.setDataset(datasetPlace);
		}

		factory.getEventBus().fireEvent(new PlaceChangeEvent(reportPlace));
	}

	private void instanceAvailable() {
		if (!started || instancePlace == null) return;

		factory.getEventBus().fireEvent(new PlaceChangeEvent(instancePlace));
	}
}
