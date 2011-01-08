package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.requests.RequestFactory;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;
import nz.ac.vuw.ecs.rprofs.client.views.impl.InspectorWidget;
import nz.ac.vuw.ecs.rprofs.client.views.impl.ReportPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;

public class ProfilerFactoryImpl implements ProfilerFactory {

	private final EventBus bus;
	private final RequestFactory rf;
	private final InspectorWidget widget;
	private final PlaceController pc;
	private final ReportView cb;

	public ProfilerFactoryImpl() {
		bus = new SimpleEventBus();
		rf = GWT.create(RequestFactory.class);
		rf.initialize(bus);

		widget = new InspectorWidget();
		pc = new PlaceController(bus);
		cb = new ReportPanel();
	}

	@Override
	public EventBus getEventBus() {
		return bus;
	}

	@Override
	public RequestFactory getRequestFactory() {
		return rf;
	}

	@Override
	public InspectorWidget getInspectorView() {
		return widget;
	}

	@Override
	public PlaceController getPlaceController() {
		return pc;
	}

	@Override
	public ReportView getClassBrowser() {
		return cb;
	}

}
