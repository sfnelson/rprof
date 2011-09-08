package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.activity.SelectDatasetActivity;
import nz.ac.vuw.ecs.rprofs.client.place.shared.HasDataset;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class DatasetActivityMapper implements ActivityMapper {

	private final SelectDatasetActivity activity; // only need one

	public DatasetActivityMapper(Factory factory) {
		this.activity = new SelectDatasetActivity(factory);
	}

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof HasDataset) {
			HasDataset p = (HasDataset) place;
			activity.setSelected(p);
		}
		else {
			activity.setSelected(null);
		}

		return activity;
	}

}
