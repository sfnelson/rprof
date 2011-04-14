package nz.ac.vuw.ecs.rprofs.client.views;

import nz.ac.vuw.ecs.rprofs.client.request.DatasetReportProxy;

import com.google.gwt.user.client.ui.IsWidget;

public interface DatasetReportView extends IsWidget {

	void clear();
	void showReport(DatasetReportProxy response);

}
