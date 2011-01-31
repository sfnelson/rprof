package nz.ac.vuw.ecs.rprofs.client.place.shared;

import nz.ac.vuw.ecs.rprofs.client.place.InspectorPlace;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryMapper;

public class PlaceHistoryHandler extends com.google.gwt.place.shared.PlaceHistoryHandler {

	public PlaceHistoryHandler(PlaceHistoryMapper mapper) {
		super(mapper);
	}

	public PlaceHistoryHandler(PlaceHistoryMapper mapper, Historian historian) {
		super(mapper, historian);
	}

	@Override
	public HandlerRegistration register(PlaceController placeController,
			final EventBus eventBus, Place defaultPlace) {

		// Wrap eventBus to catch PlaceChangeEvents and prevent history changes
		// for places which aren't InspectorPlaces.
		EventBus wrappedBus = new EventBus() {

			@Override
			public <H extends EventHandler> HandlerRegistration addHandler(
					Type<H> type, H handler) {
				final PlaceChangeEvent.Handler h = (PlaceChangeEvent.Handler) handler;
				if (type.equals(PlaceChangeEvent.TYPE)) {
					return eventBus.addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {
						@Override
						public void onPlaceChange(PlaceChangeEvent event) {
							if (event.getNewPlace() instanceof InspectorPlace) {
								h.onPlaceChange(event);
							}
						}
					});
				}
				else {
					return eventBus.addHandler(type, handler);
				}
			}

			@Override
			public <H extends EventHandler> HandlerRegistration addHandlerToSource(
					Type<H> type, Object source, H handler) {
				return eventBus.addHandlerToSource(type, source, handler);
			}

			@Override
			public void fireEvent(GwtEvent<?> event) {
				eventBus.fireEvent(event);
			}

			@Override
			public void fireEventFromSource(GwtEvent<?> event, Object source) {
				eventBus.fireEventFromSource(event, source);
			}

		};
		return super.register(placeController, wrappedBus, defaultPlace);
	}

}
