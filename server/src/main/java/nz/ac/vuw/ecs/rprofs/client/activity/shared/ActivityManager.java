package nz.ac.vuw.ecs.rprofs.client.activity.shared;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;

public class ActivityManager extends com.google.gwt.activity.shared.ActivityManager {

	private final Class<? extends Place> placeType;

	public ActivityManager(ActivityMapper mapper, EventBus eventBus, Class<? extends Place> placeType) {
		super(mapper, eventBus);

		this.placeType = placeType;
	}

	@Override
	public void onPlaceChange(PlaceChangeEvent event) {
		Place place = event.getNewPlace();
		Class<?> type = place.getClass();

		while (type != null && !placeType.equals(type)) {
			type = type.getSuperclass();
		}

		if (type == null) {
			return;
		}

		super.onPlaceChange(event);
	}
}
