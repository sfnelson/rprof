package nz.ac.vuw.ecs.rprofs.client.views;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;


public interface InspectorView extends IsWidget {

	AcceptsOneWidget getDatasetContainer();
	AcceptsOneWidget getReportContainer();
	AcceptsOneWidget getEventContainer();

}
