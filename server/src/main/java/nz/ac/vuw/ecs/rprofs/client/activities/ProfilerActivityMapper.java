package nz.ac.vuw.ecs.rprofs.client.activities;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.places.InspectorPlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class ProfilerActivityMapper implements ActivityMapper {

	private ProfilerFactory factory;

	public ProfilerActivityMapper(ProfilerFactory factory) {
		this.factory = factory;
	}

	@Override
	public Activity getActivity(Place place) {
		InspectorPlace p;
		if (place instanceof InspectorPlace) {
			p = (InspectorPlace) place;
		}
		else {
			p = new InspectorPlace(null, null);
		}

		return new InspectorActivity(p, factory);
	}

}
