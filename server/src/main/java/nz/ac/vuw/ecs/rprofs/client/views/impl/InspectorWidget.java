package nz.ac.vuw.ecs.rprofs.client.views.impl;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
import nz.ac.vuw.ecs.rprofs.client.views.ProfilerAppView;
import nz.ac.vuw.ecs.rprofs.client.views.ReportSelectorView;

public class InspectorWidget extends FrameLayout implements ProfilerAppView {

	private final ReportSelectorView reportPanel;

	@Inject
	public InspectorWidget(PlaceController pc, EventBus bus) {
		super(FrameLayout.MAX_HEIGHT | FrameLayout.HIDE_BOTTOM, 15, 50, Unit.PCT);

		reportPanel = new ReportSelectionPanel(pc, bus, this);
		getCenter().setWidget(reportPanel);
	}

	@Override
	public AcceptsOneWidget getDatasetContainer() {
		return getTop();
	}

	@Override
	public AcceptsOneWidget getReportContainer() {
		return reportPanel;
	}

	@Override
	public AcceptsOneWidget getInspectorContainer() {
		return getBottom();
	}

}
