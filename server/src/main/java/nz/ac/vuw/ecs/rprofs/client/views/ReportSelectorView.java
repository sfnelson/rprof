package nz.ac.vuw.ecs.rprofs.client.views;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public interface ReportSelectorView extends IsWidget, AcceptsOneWidget {

	void setSelected(String report);

}
