package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.activities.ProfilerActivityMapper;
import nz.ac.vuw.ecs.rprofs.client.places.InspectorPlace;
import nz.ac.vuw.ecs.rprofs.client.places.ProfilerPlaceHistoryMapper;

import com.google.gwt.activity.shared.ActivityManager;
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

		ProfilerActivityMapper activityMapper = new ProfilerActivityMapper(factory);
		ActivityManager activityManager = new ActivityManager(activityMapper, bus);
		activityManager.setDisplay(container);

		ProfilerPlaceHistoryMapper historyMapper = new ProfilerPlaceHistoryMapper();
		PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
		historyHandler.register(controller, bus, new InspectorPlace(null, null));

		RootPanel.get().add(container);
		container.add(factory.getInspectorView());
		historyHandler.handleCurrentHistory();
	}
}
