package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.requests.RequestFactory;
import nz.ac.vuw.ecs.rprofs.client.views.InspectorView;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;

public interface ProfilerFactory {

	EventBus getEventBus();
	RequestFactory getRequestFactory();
	InspectorView getInspectorView();
	PlaceController getPlaceController();
	ReportView getClassBrowser();

}
