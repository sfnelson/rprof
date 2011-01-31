package nz.ac.vuw.ecs.rprofs.client.views.impl;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
import nz.ac.vuw.ecs.rprofs.client.views.InspectorView;
import nz.ac.vuw.ecs.rprofs.client.views.ReportSelectorView;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class InspectorWidget extends FrameLayout implements InspectorView {

	private final ReportSelectorView reportPanel;

	public InspectorWidget(ProfilerFactory factory) {
		super(FrameLayout.MAX_HEIGHT | FrameLayout.HIDE_BOTTOM, 15, 50, Unit.PCT);

		reportPanel = new ReportSelectionPanel(factory, this);
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
	public AcceptsOneWidget getEventContainer() {
		return getBottom();
	}

}
