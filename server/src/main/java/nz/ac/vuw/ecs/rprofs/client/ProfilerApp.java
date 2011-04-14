package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.views.ProfilerAppView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ProfilerApp extends AbstractActivity {

	private final Factory factory;
	private final ProfilerAppView view;

	public ProfilerApp(Factory factory) {
		this.factory = factory;
		this.view = factory.getProfilerView();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		DatasetActivityMapper datasetMapper = new DatasetActivityMapper(factory);
		new ActivityManager(datasetMapper, eventBus).setDisplay(view.getDatasetContainer());

		InspectorActivityMapper reportMapper = new InspectorActivityMapper(factory);
		new ActivityManager(reportMapper, eventBus).setDisplay(view.getReportContainer());

		ReportActivityMapper inspectorMapper = new ReportActivityMapper(factory);
		new ActivityManager(inspectorMapper, eventBus).setDisplay(view.getInspectorContainer());

		panel.setWidget(view);
	}

}
