package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import nz.ac.vuw.ecs.rprofs.client.place.SelectDataset;

public class AppEntryPoint implements EntryPoint {

	private final ProfilerInjector injector = GWT.create(ProfilerInjector.class);

	@Override
	public void onModuleLoad() {
		EventBus eventBus = injector.getEventBus();

		PlaceController pc = injector.getPlaceController();

		HistoryMapper hm = GWT.create(HistoryMapper.class);
		PlaceHistoryHandler hh = new PlaceHistoryHandler(hm);
		hh.register(pc, (com.google.web.bindery.event.shared.EventBus) eventBus, new SelectDataset());

		injector.getProfilerApp().start(new AcceptsOneWidget() {
			@Override
			public void setWidget(IsWidget child) {
				RootPanel.get().add(child);
			}
		}, eventBus);

		hh.handleCurrentHistory();
	}

}
