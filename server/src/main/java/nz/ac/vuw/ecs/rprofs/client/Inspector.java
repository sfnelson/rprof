package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.activity.shared.ActivityManager;
import nz.ac.vuw.ecs.rprofs.client.place.DatasetPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InspectorPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InstancePlace;
import nz.ac.vuw.ecs.rprofs.client.place.ReportPlace;
import nz.ac.vuw.ecs.rprofs.client.place.shared.InspectorPlaceHistoryMapper;
import nz.ac.vuw.ecs.rprofs.client.views.InspectorView;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Inspector implements EntryPoint {

	private static Inspector instance;
	public static Inspector getInstance() {
		return instance;
	}

	{
		instance = this;
	}

	public void onModuleLoad() {
		SimplePanel container = new SimplePanel();
		container.setStyleName("wrapper");

		ProfilerFactory factory = GWT.create(ProfilerFactory.class);
		EventBus bus = factory.getEventBus();
		PlaceController controller = factory.getPlaceController();

		ActivityMapper am = factory.getActivityMapper();

		new ActivityManager(am, bus, InspectorPlace.class).setDisplay(container);

		InspectorView view = factory.getInspectorView();
		new ActivityManager(am, bus, DatasetPlace.class).setDisplay(view.getDatasetContainer());
		new ActivityManager(am, bus, ReportPlace.class).setDisplay(view.getReportContainer());
		new ActivityManager(am, bus, InstancePlace.class).setDisplay(view.getEventContainer());

		InspectorPlaceHistoryMapper historyMapper = new InspectorPlaceHistoryMapper(factory);
		PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
		historyHandler.register(controller, bus, new InspectorPlace());

		RootPanel.get().add(container);
		historyHandler.handleCurrentHistory();
	}
}
