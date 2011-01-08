package nz.ac.vuw.ecs.rprofs.client.views.impl;

import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
import nz.ac.vuw.ecs.rprofs.client.views.InspectorView;
import nz.ac.vuw.ecs.rprofs.client.views.ReportManagerView;

import com.google.gwt.dom.client.Style.Unit;

public class InspectorWidget extends FrameLayout implements InspectorView {

	private DatasetPanel datasetPanel;
	private ReportManagerPanel reportPanel;

	public InspectorWidget() {
		super(FrameLayout.MAX_HEIGHT | FrameLayout.HIDE_BOTTOM, 15, 50, Unit.PCT);

		datasetPanel = new DatasetPanel();
		reportPanel = new ReportManagerPanel(this);

		setTop(datasetPanel);
		setCenter(reportPanel);
		//layout.setBottom(new InstanceInspector(this));
	}

	@Override
	public DatasetPanel getDatasetListView() {
		return datasetPanel;
	}

	@Override
	public ReportManagerView getReportManagerView() {
		return reportPanel;
	}

}
