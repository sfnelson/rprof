package nz.ac.vuw.ecs.rprofs.client.views;

import nz.ac.vuw.ecs.rprofs.client.place.ReportPlace;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public interface ReportSelectorView extends IsWidget, AcceptsOneWidget {

	void setSelected(ReportPlace<?> report);

}
