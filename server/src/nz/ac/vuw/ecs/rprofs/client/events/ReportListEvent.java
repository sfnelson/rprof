/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.events;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.Report;

import com.google.gwt.event.shared.GwtEvent;

public class ReportListEvent extends GwtEvent<ReportListHandler> {

	private static Type<ReportListHandler> TYPE = new Type<ReportListHandler>();

	private final List<Report> reports;
	public ReportListEvent(List<Report> reports) {
		this.reports = Collections.immutable(reports);
	}

	@Override
	protected void dispatch(ReportListHandler handler) {
		handler.onReportsAvailable(reports);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ReportListHandler> getAssociatedType() {
		return TYPE;
	}

	public static Type<ReportListHandler> getType() {
		return TYPE;
	}
}