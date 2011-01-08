package nz.ac.vuw.ecs.rprofs.client.activities;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.places.InspectorPlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class InspectorActivity implements Activity {

	private DatasetActivity dataset;
	private ReportManagerActivity report;

	public InspectorActivity(InspectorPlace place, ProfilerFactory factory) {
		dataset = new DatasetActivity(factory, place.getDatasetHandle());
		report = new ReportManagerActivity(factory, place.getReportHandle());
	}

	@Override
	public String mayStop() {
		return null;
	}

	@Override
	public void onCancel() {
		dataset.onCancel();
		report.onCancel();
	}

	@Override
	public void onStop() {
		dataset.onStop();
		report.onStop();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		dataset.start(null, eventBus);
		report.start(null, eventBus);
	}

}
