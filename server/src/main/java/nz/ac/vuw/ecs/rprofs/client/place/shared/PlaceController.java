package nz.ac.vuw.ecs.rprofs.client.place.shared;

import nz.ac.vuw.ecs.rprofs.client.place.InspectorPlace;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;

public class PlaceController extends com.google.gwt.place.shared.PlaceController {

	public PlaceController(EventBus eventBus) {
		super(eventBus);
	}

	@Override
	public void goTo(Place newPlace) {
		System.out.println("goto: [" + newPlace + "] (" + newPlace.getClass() + ")");

		newPlace = getCurrent().setPlace(newPlace);

		super.goTo(newPlace);
	}

	public InspectorPlace getCurrent() {
		if (getWhere() instanceof InspectorPlace) {
			return (InspectorPlace) getWhere();
		}
		else {
			return InspectorPlace.NOWHERE;
		}
	}

}
