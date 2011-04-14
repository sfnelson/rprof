package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.place.SelectDataset;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

public class AppEntryPoint implements EntryPoint {

	@Override
	public void onModuleLoad() {
		Factory factory = GWT.create(Factory.class);
		EventBus eventBus = factory.getEventBus();
		PlaceController pc = factory.getPlaceController();

		HistoryMapper hm = GWT.create(HistoryMapper.class);
		PlaceHistoryHandler hh = new PlaceHistoryHandler(hm);
		hh.register(pc, eventBus, new SelectDataset());

		new ProfilerApp(factory).start(new AcceptsOneWidget() {
			@Override
			public void setWidget(IsWidget child) {
				RootPanel.get().add(child);
			}
		}, eventBus);

		hh.handleCurrentHistory();
	}

}
