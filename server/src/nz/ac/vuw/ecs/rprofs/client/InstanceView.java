/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceView extends Composite implements ReportListener<Entry>, ClickHandler, HasEntries, View {

	private static InstanceViewUiBinder uiBinder = GWT.create(InstanceViewUiBinder.class);
	interface InstanceViewUiBinder extends UiBinder<Widget, InstanceView> {}
	interface Style extends CssResource {
		String even();
	}
	
	private final Inspector server;
	private final Report report;
	private final Button button;

	@UiField Style style;
	@UiField Panel container;
	@UiField Entry background;
	@UiField Entry heading;

	public InstanceView(Inspector server, Report report) {
		this.button = new Button(report.name);
		this.server = server;
		this.report = report;
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void refresh() {
		container.clear();
		server.getReportStatus(report, this);
	}

	@Override
	public void onClick(ClickEvent event) {
		Entry entry = ((Entry.EntryPanel) event.getSource()).parent();
		if (entry.isOpen()) {
			entry.close();
		}
		else {
			if (!entry.isPopulated()) {
				server.getReportData(report, entry.getTarget(), entry, this);
			}
			entry.open();
		}
	}

	@Override
	public void statusUpdate(Status status) {
		switch (status.state) {
		case UNINITIALIZED:
			server.generateReport(report, this);
			break;
		case GENERATING:
			ErrorPanel.showMessage("generating: " + status.progress);
			break;
		case READY:
			server.getReportData(report, null, null, this);
			break;
		}
	}

	@Override
	public void dataAvailable(Report.Entry key, Entry target, int entries, ReportCallback<Entry> callback) {
		if (target == null) {
			callback.getData(key, target, 0, entries);
		}
		else {
			callback.getData(key, target, 0, 50);
		}
	}

	@Override
	public void handleData(Report.Entry key, Entry target, int offset, int limit, List<? extends Report.Entry> result,
			ReportCallback<Entry> callback) {

		HasEntries container;
		if (target == null) {
			container = this;
		}
		else {
			container = target;
		}

		for (Report.Entry r: result) {
			Entry e = new Entry(report, r);
			e.addClickHandler(this);
			container.add(e);
		}
	}

	@Override
	public void add(Entry entry) {
		container.add(entry);
	}
	
	@Override
	public Widget getContentItem() {
		return this;
	}

	@Override
	public Button getMenuButton() {
		return button;
	}
	
	@UiFactory
	Entry createHeading() {
		return new Entry(report);
	}
}
