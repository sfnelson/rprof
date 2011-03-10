package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.activity.shared.ActivityMapper;
import nz.ac.vuw.ecs.rprofs.client.place.shared.PlaceController;
import nz.ac.vuw.ecs.rprofs.client.request.RequestFactory;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetSelectorView;
import nz.ac.vuw.ecs.rprofs.client.views.EventReportView;
import nz.ac.vuw.ecs.rprofs.client.views.EventView;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;
import nz.ac.vuw.ecs.rprofs.client.views.impl.DatasetPanel;
import nz.ac.vuw.ecs.rprofs.client.views.impl.EventPanel;
import nz.ac.vuw.ecs.rprofs.client.views.impl.EventReportPanel;
import nz.ac.vuw.ecs.rprofs.client.views.impl.InspectorWidget;
import nz.ac.vuw.ecs.rprofs.client.views.impl.ReportPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

public class ProfilerFactoryImpl implements ProfilerFactory {

	private final EventBus eventBus;
	private final RequestFactory requestFactory;
	private final InspectorWidget inspector;

	private final PlaceController placeController;
	private final ActivityMapper activityMapper;

	private final DatasetSelectorView datasetView;
	private final ReportView reportView;
	private final EventView eventView;
	private final EventReportView eventReportView;

	public ProfilerFactoryImpl() {
		eventBus = new SimpleEventBus();
		requestFactory = GWT.create(RequestFactory.class);
		requestFactory.initialize(eventBus);

		placeController = new PlaceController(eventBus);
		activityMapper = new ActivityMapper(this);

		inspector = new InspectorWidget(this);

		datasetView = new DatasetPanel();
		reportView = new ReportPanel();
		eventView = new EventPanel();
		eventReportView = new EventReportPanel();
	}

	@Override
	public RequestFactory getRequestFactory() {
		return requestFactory;
	}

	@Override
	public InspectorWidget getInspectorView() {
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
	public ActivityMapper getActivityMapper() {
		return activityMapper;
	}

	@Override
	public DatasetSelectorView getDatasetView() {
		return datasetView;
	}

	@Override
	public EventView getEventView() {
		return eventView;
	}

	@Override
	public ReportView getReportView() {
		return reportView;
	}

	@Override
	public EventReportView getEventReportView() {
		return eventReportView;
	}

}
