package nz.ac.vuw.ecs.rprofs.client.activities;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.places.InspectorPlace;
import nz.ac.vuw.ecs.rprofs.client.requests.ReportProxy;
import nz.ac.vuw.ecs.rprofs.client.views.ReportManagerView;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ReportManagerActivity extends ListActivity<ReportProxy> implements ReportManagerView.Presenter {

	private final ProfilerFactory factory;
	private final String selectedReport;

	private Activity report;

	private ReportManagerView view;

	public ReportManagerActivity(ProfilerFactory factory, String currentReport) {
		this.factory = factory;
		this.selectedReport = currentReport;

		if ("classes".equals(currentReport)) {
			report = new ClassBrowserActivity(factory);
		}
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		this.view = factory.getInspectorView().getReportManagerView();
		this.view.setPresenter(this);

		if (report != null) {
			report.start(view.getReportContainer(), eventBus);
		}

		refresh();
	}

	@Override
	public String mayStop() {
		return null;
	}

	@Override
	public void onCancel() {
		onStop();

		if (report != null) {
			report.onCancel();
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		if (report != null) {
			report.onStop();
		}

		view = null;
	}

	private void refresh() {
		refresh(factory.getRequestFactory().reportRequest().findAllReports());
	}

	@Override
	public void selectReport(ReportProxy report) {
		view.selectReport(report);
		factory.getPlaceController().goTo(getLink(report));
	}

	@Override
	public String updateLink(ReportProxy report) {
		return "#" + getLink(report).toString();
	}

	private Place getLink(ReportProxy report) {
		Place place = factory.getPlaceController().getWhere();
		if (place instanceof InspectorPlace) {
			place = ((InspectorPlace) place).setReport(report);
		}
		else {
			place = new InspectorPlace(null, report);
		}
		return place;
	}

	@Override
	protected void addEntry(ReportProxy e) {
		view.addReport(e);
		if (e.getReference().equals(selectedReport)) {
			view.selectReport(e);
		}
	}

	@Override
	protected void updateEntry(ReportProxy e) {
		view.updateReport(e);
	}

	@Override
	protected void removeEntry(ReportProxy e) {
		view.removeReport(e);
	}

}
