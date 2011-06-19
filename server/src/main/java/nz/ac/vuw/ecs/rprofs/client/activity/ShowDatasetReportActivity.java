package nz.ac.vuw.ecs.rprofs.client.activity;

import nz.ac.vuw.ecs.rprofs.client.Factory;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetReportProxy;
import nz.ac.vuw.ecs.rprofs.client.request.ReportRequest;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetReportView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class ShowDatasetReportActivity extends AbstractActivity {

	private final Factory factory;
	private final DatasetReportView view;

	private boolean started;
	private String datasetHandle;

	public ShowDatasetReportActivity(Factory factory) {
		this.factory = factory;
		this.view = factory.getDatasetReportView();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view);
		started = true;

		init();
	}

	public String getDataset() {
		return datasetHandle;
	}

	public void setDataset(String dataset) {
		this.datasetHandle = dataset;

		init();
	}

	private void init() {
		System.out.println("init: " + started + " " + datasetHandle);
		if (!started || datasetHandle == null) return;

		ReportRequest rq = factory.getRequestFactory().reportRequest();
		System.out.println("requesting report");
		rq.getDatasetReport().fire(new Receiver<DatasetReportProxy>() {
			@Override
			public void onSuccess(DatasetReportProxy response) {
				System.out.println("report received");
				setReport(response);
			}
		});
	}

	private void setReport(DatasetReportProxy response) {
		view.clear();
		view.showReport(response);
	}
}
