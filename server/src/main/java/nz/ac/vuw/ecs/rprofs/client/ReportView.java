/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.history.History;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ReportView implements View {
	
	private final Report report;
	
	public ReportView(Report report) {
		this.report = report;
	}

	@Override
	public Widget createWidget(History history) {
		return new ReportPanel(history.run, report);
	}

	@Override
	public String getDescription() {
		return report.description;
	}

	@Override
	public String getIdentifier() {
		return report.identifier;
	}

	@Override
	public String getTitle() {
		return report.title;
	}

}
