package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ProfilerPlace;

public class AppEntryPoint implements EntryPoint {

	private final ProfilerInjector injector = GWT.create(ProfilerInjector.class);

	@Override
	public void onModuleLoad() {
		EventBus eventBus = injector.getEventBus();

		PlaceController pc = injector.getPlaceController();

		HistoryMapper hm = injector.getHistoryMapper();

		PlaceHistoryHandler hh = new PlaceHistoryHandler(hm);
		hh.register(pc, eventBus, new ProfilerPlace());

		injector.getProfilerApp().start(new AcceptsOneWidget() {
			@Override
			public void setWidget(IsWidget child) {
				RootPanel.get().add(child);
			}
		}, (com.google.gwt.event.shared.EventBus) eventBus);

		hh.handleCurrentHistory();
	}

}
