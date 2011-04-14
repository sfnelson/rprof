package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.activity.ShowDatasetReportActivity;
import nz.ac.vuw.ecs.rprofs.client.place.shared.HasDataset;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class ReportActivityMapper implements ActivityMapper {

	private final Factory factory;
	private Activity current;

	public ReportActivityMapper(Factory factory) {
		this.factory = factory;
	}

	@Override
	public Activity getActivity(Place place) {
		Activity next = null;

		if (place instanceof HasDataset) {
			String handle = ((HasDataset) place).getDatasetHandle();
			ShowDatasetReportActivity activity;

			if (current != null && current instanceof ShowDatasetReportActivity) {
				activity = (ShowDatasetReportActivity) current;
				if (!activity.getDataset().equals(handle)) {
					activity.setDataset(handle);
				}
			}
			else {
				activity = new ShowDatasetReportActivity(factory);
				activity.setDataset(handle);
			}

			next = activity;
		}

		current = next;
		return next;
	}
}