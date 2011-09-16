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
import nz.ac.vuw.ecs.rprofs.client.place.HasView;

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
		if (place instanceof HasView) {
			HasView p = (HasView) place;
			String view = p.getView();

			if ("classes".equals(view)) {
				return inspectClasses.get().setPlace(p);
			}
			if ("instances".equals(view)) {
				return inspectInstances.get().setPlace(p);
			}
			if ("fields".equals(view)) {
				return inspectFields.get().setPlace(p);
			}
			if ("events".equals(view)) {
				return inspectEvents.get().setPlace(p);
			}
		}

		return null;
	}

}