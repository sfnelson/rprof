package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;
import com.google.inject.Provider;
import nz.ac.vuw.ecs.rprofs.client.activity.*;
import nz.ac.vuw.ecs.rprofs.client.place.HasView;

public class InspectorActivityMapper implements ActivityMapper {

	private final SelectView selectView;

	private final Provider<InspectClassesActivity> inspectClasses;
	private final Provider<InspectInstancesActivity> inspectInstances;
	private final Provider<InspectFieldsActivity> inspectFields;
	private final Provider<InspectEventsActivity> inspectEvents;

	@Inject
	public InspectorActivityMapper(SelectView selectView,
								   Provider<InspectClassesActivity> inspectClasses,
								   Provider<InspectInstancesActivity> inspectInstances,
								   Provider<InspectFieldsActivity> inspectFields,
								   Provider<InspectEventsActivity> inspectEvents) {
		this.selectView = selectView;

		this.inspectClasses = inspectClasses;
		this.inspectInstances = inspectInstances;
		this.inspectFields = inspectFields;
		this.inspectEvents = inspectEvents;

		selectView.start(null, null);
	}

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof HasView) {
			HasView p = (HasView) place;
			String view = p.getView();

			selectView.setPlace(p);

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