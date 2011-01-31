package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.place.shared.PlaceController;
import nz.ac.vuw.ecs.rprofs.client.requests.RequestFactory;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetSelectorView;
import nz.ac.vuw.ecs.rprofs.client.views.EventView;
import nz.ac.vuw.ecs.rprofs.client.views.InspectorView;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.event.shared.EventBus;

public interface ProfilerFactory {

	RequestFactory getRequestFactory();

	EventBus getEventBus();
	PlaceController getPlaceController();
	ActivityMapper getActivityMapper();

	InspectorView getInspectorView();
	DatasetSelectorView getDatasetView();
	ReportView getReportView();
	EventView getEventView();

}
