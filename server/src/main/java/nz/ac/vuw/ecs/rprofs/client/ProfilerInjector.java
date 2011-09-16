package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import nz.ac.vuw.ecs.rprofs.client.place.ProfilerPlaceFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/09/11
 */
@GinModules(ProfilerModule.class)
public interface ProfilerInjector extends Ginjector {
	EventBus getEventBus();

	ProfilerApp getProfilerApp();

	PlaceController getPlaceController();

	ProfilerPlaceFactory getPlaceFactory();

	HistoryMapper getHistoryMapper();
}
