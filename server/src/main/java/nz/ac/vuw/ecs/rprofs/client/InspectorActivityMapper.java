package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.activity.InspectClassesActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.InspectEventsActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.InspectFieldsActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.InspectInstancesActivity;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseClasses;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseEvents;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseFields;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseInstances;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class InspectorActivityMapper implements ActivityMapper {

	private final Factory factory;

	public InspectorActivityMapper(Factory factory) {
		this.factory = factory;
	}

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof BrowseClasses) {
			return new InspectClassesActivity(factory, (BrowseClasses) place);
		}
		if (place instanceof BrowseInstances) {
			return new InspectInstancesActivity(factory, (BrowseInstances) place);
		}
		if (place instanceof BrowseFields) {
			return new InspectFieldsActivity(factory, (BrowseFields) place);
		}
		if (place instanceof BrowseEvents) {
			return new InspectEventsActivity(factory, (BrowseEvents) place);
		}

		return null;
	}

}