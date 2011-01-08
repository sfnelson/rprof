package nz.ac.vuw.ecs.rprofs.client.views;

import com.google.gwt.user.client.ui.IsWidget;


public interface InspectorView extends IsWidget {

	DatasetListView getDatasetListView();
	ReportManagerView getReportManagerView();

}
