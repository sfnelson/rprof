package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import nz.ac.vuw.ecs.rprofs.client.activity.SelectDatasetActivity;
import nz.ac.vuw.ecs.rprofs.client.place.shared.HasDataset;
import nz.ac.vuw.ecs.rprofs.client.request.id.DatasetIdProxy;

import javax.inject.Provider;

public class DatasetActivityMapper implements ActivityMapper {

	private final EventBus eventBus;
	private final Provider<SelectDatasetActivity> selectActivity;

	private DatasetIdProxy dataset;

	@Inject
	public DatasetActivityMapper(EventBus bus, Provider<SelectDatasetActivity> selectActivity) {
		this.eventBus = bus;
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
