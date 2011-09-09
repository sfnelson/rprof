package nz.ac.vuw.ecs.rprofs.client.views;

import com.google.gwt.user.client.ui.IsWidget;

public interface DatasetReportView extends IsWidget {

	void clear();
	void showReport(Object response);

}
