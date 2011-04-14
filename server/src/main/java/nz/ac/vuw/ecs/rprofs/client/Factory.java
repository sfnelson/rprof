package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.request.RequestFactory;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetReportView;
import nz.ac.vuw.ecs.rprofs.client.views.EventView;
import nz.ac.vuw.ecs.rprofs.client.views.ProfilerAppView;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;

public interface Factory {

	EventBus getEventBus();
	RequestFactory getRequestFactory();
	PlaceController getPlaceController();

	ProfilerAppView getProfilerView();

	DatasetListView getDatasetView();
	ReportView getReportView();
	EventView getEventView();

	DatasetReportView getDatasetReportView();
}
