package nz.ac.vuw.ecs.rprofs.client.views.impl;

import nz.ac.vuw.ecs.rprofs.client.request.DatasetReportProxy;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetReportView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DatasetReportPanel extends Composite implements DatasetReportView {

	private static DatabaseReportPanelUiBinder uiBinder = GWT
	.create(DatabaseReportPanelUiBinder.class);

	interface DatabaseReportPanelUiBinder extends
	UiBinder<Widget, DatasetReportPanel> {
	}

	@UiField Label numClasses;
	@UiField Label numObjects;
	@UiField Label meanObjectsPerClass;
	@UiField Label sdObjectsPerClass;
	@UiField Label meanWritesPerClass;
	@UiField Label sdWritesPerClass;
	@UiField Label meanWritesPerObject;
	@UiField Label sdWritesPerObject;
	@UiField Label meanReadsPerClass;
	@UiField Label sdReadsPerClass;
	@UiField Label meanReadsPerObject;
	@UiField Label sdReadsPerObject;

	public DatasetReportPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void clear() {

	}

	@Override
	public void showReport(DatasetReportProxy response) {
		numClasses.setText(String.valueOf(response.getNumClasses()));
		numObjects.setText(String.valueOf(response.getNumObjects()));

		meanObjectsPerClass.setText(String.valueOf(response.getObjectsPerClass().getMean()));
		sdObjectsPerClass.setText(String.valueOf(response.getObjectsPerClass().getStdDev()));

		meanWritesPerClass.setText(String.valueOf(response.getWritesPerClass().getMean()));
		sdWritesPerClass.setText(String.valueOf(response.getWritesPerClass().getStdDev()));
		meanWritesPerObject.setText(String.valueOf(response.getWritesPerObject().getMean()));
		sdWritesPerObject.setText(String.valueOf(response.getWritesPerObject().getStdDev()));

		meanReadsPerClass.setText(String.valueOf(response.getReadsPerClass().getMean()));
		sdReadsPerClass.setText(String.valueOf(response.getReadsPerClass().getStdDev()));
		meanReadsPerObject.setText(String.valueOf(response.getReadsPerObject().getMean()));
		sdReadsPerObject.setText(String.valueOf(response.getReadsPerObject().getStdDev()));
	}

}
