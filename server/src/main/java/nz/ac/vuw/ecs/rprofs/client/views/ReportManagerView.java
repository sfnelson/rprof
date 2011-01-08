package nz.ac.vuw.ecs.rprofs.client.views;

import nz.ac.vuw.ecs.rprofs.client.requests.ReportProxy;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public interface ReportManagerView extends IsWidget {

	void setPresenter(Presenter presenter);

	void addReport(ReportProxy report);
	void updateReport(ReportProxy report);
	void selectReport(ReportProxy report);
	void removeReport(ReportProxy report);

	AcceptsOneWidget getReportContainer();

	public interface Presenter {
		void selectReport(ReportProxy report);
		String updateLink(ReportProxy report);
	}
}
