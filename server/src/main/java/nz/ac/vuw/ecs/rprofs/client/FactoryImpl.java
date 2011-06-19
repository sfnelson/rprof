package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.request.RequestFactory;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetReportView;
import nz.ac.vuw.ecs.rprofs.client.views.EventView;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;
import nz.ac.vuw.ecs.rprofs.client.views.impl.DatasetPanel;
import nz.ac.vuw.ecs.rprofs.client.views.impl.DatasetReportPanel;
import nz.ac.vuw.ecs.rprofs.client.views.impl.EventPanel;
import nz.ac.vuw.ecs.rprofs.client.views.impl.InspectorWidget;
import nz.ac.vuw.ecs.rprofs.client.views.impl.ReportPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.requestfactory.shared.RequestTransport;

public class FactoryImpl implements Factory {

	private final EventBus eventBus;
	private final RequestFactory requestFactory;
	private final RequestTransport transport;
	private final InspectorWidget inspector;

	private final PlaceController placeController;

	private final DatasetListView datasetView;
	private final ReportView reportView;
	private final EventView eventView;

	public FactoryImpl() {
		eventBus = new SimpleEventBus();

		requestFactory = GWT.create(RequestFactory.class);
		transport = new DatasetAwareRequestTransport(eventBus);
		requestFactory.initialize(eventBus, transport);

		placeController = new PlaceController(eventBus);

		inspector = new InspectorWidget(this);

		datasetView = new DatasetPanel();
		reportView = new ReportPanel();
		eventView = new EventPanel();
	}

	@Override
	public RequestFactory getRequestFactory() {
		return requestFactory;
	}

	@Override
	public InspectorWidget getProfilerView() {
		return inspector;
	}

	@Override
	public PlaceController getPlaceController() {
		return placeController;
	}

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	@Override
	public DatasetListView getDatasetView() {
		return datasetView;
	}

	@Override
	public ReportView getReportView() {
		return reportView;
	}

	@Override
	public EventView getEventView() {
		return eventView;
	}

	@Override
	public DatasetReportView getDatasetReportView() {
		return new DatasetReportPanel();
	}

}
