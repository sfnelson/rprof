package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.client.activity.SelectDatasetActivity;
import nz.ac.vuw.ecs.rprofs.client.place.HasDataset;

import javax.inject.Provider;

public class DatasetActivityMapper implements ActivityMapper {

	private final Provider<SelectDatasetActivity> selectActivity;

	@Inject
	public DatasetActivityMapper(Provider<SelectDatasetActivity> selectActivity) {
		this.selectActivity = selectActivity;
	}

	@Override
	public Activity getActivity(Place place) {
		Activity activity = null;

		if (place instanceof HasDataset) {
			HasDataset p = (HasDataset) place;
			activity = selectActivity.get().setPlace(p);
		}

		return activity;
	}

}
