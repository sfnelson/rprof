package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;
import com.google.inject.Provider;
import nz.ac.vuw.ecs.rprofs.client.activity.InspectClassesActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.InspectEventsActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.InspectFieldsActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.InspectInstancesActivity;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseClasses;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseEvents;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseFields;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseInstances;

public class InspectorActivityMapper implements ActivityMapper {

	private final Provider<InspectClassesActivity> inspectClasses;
	private final Provider<InspectInstancesActivity> inspectInstances;
	private final Provider<InspectFieldsActivity> inspectFields;
	private final Provider<InspectEventsActivity> inspectEvents;

	@Inject
	public InspectorActivityMapper(Provider<InspectClassesActivity> inspectClasses,
								   Provider<InspectInstancesActivity> inspectInstances,
								   Provider<InspectFieldsActivity> inspectFields,
								   Provider<InspectEventsActivity> inspectEvents) {
		this.inspectClasses = inspectClasses;
		this.inspectInstances = inspectInstances;
		this.inspectFields = inspectFields;
		this.inspectEvents = inspectEvents;
	}

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof BrowseClasses) {
			return inspectClasses.get().setPlace((BrowseClasses) place);
		}
		if (place instanceof BrowseInstances) {
			return inspectInstances.get().setPlace((BrowseInstances) place);
		}
		if (place instanceof BrowseFields) {
			return inspectFields.get().setPlace((BrowseFields) place);
		}
		if (place instanceof BrowseEvents) {
			return inspectEvents.get().setPlace((BrowseEvents) place);
		}

		return null;
	}

}