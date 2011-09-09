package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.client.views.ProfilerAppView;

public class ProfilerApp extends AbstractActivity {

	private final DatasetActivityMapper datasetMapper;
	private final InspectorActivityMapper inspectorMapper;
	private final ReportActivityMapper reportMapper;

	private final ProfilerAppView view;

	@Inject
	public ProfilerApp(DatasetActivityMapper datasetMapper, InspectorActivityMapper inspectorMapper,
					   ReportActivityMapper reportMapper, ProfilerAppView view) {
		this.datasetMapper = datasetMapper;
		this.inspectorMapper = inspectorMapper;
		this.reportMapper = reportMapper;
		this.view = view;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		new ActivityManager(datasetMapper, eventBus).setDisplay(view.getDatasetContainer());
		new ActivityManager(reportMapper, eventBus).setDisplay(view.getReportContainer());
		new ActivityManager(inspectorMapper, eventBus).setDisplay(view.getInspectorContainer());

		panel.setWidget(view);

		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable e) {
				e = unpack(e);
				GWT.log(e.getMessage(), e);
			}

			private Throwable unpack(Throwable e) {
				if (e instanceof UmbrellaException) {
					UmbrellaException u = (UmbrellaException) e;
					if (u.getCauses().size() == 1) {
						return u.getCauses().iterator().next();
					}
				}
				return e;
			}
		});
	}

}
