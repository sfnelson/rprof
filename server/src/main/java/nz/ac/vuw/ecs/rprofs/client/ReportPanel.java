package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.RunData;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Status;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ReportPanel extends Composite implements ReportListener<Entry>, MouseDownHandler, ClickHandler, HasEntries {

	private static ReportPanelUiBinder uiBinder = GWT.create(ReportPanelUiBinder.class);
	interface ReportPanelUiBinder extends UiBinder<Widget, ReportPanel> {}
	interface Style extends CssResource {
		String even();
		String refresh();
	}

	private final RunData run;
	private final Report report;
	
	private int numEntries;

	@UiField Style style;
	@UiField Panel container;
	@UiField Entry background;
	@UiField Entry heading;
	@UiField ProgressBar progress;

	public ReportPanel(final RunData run, final Report report) {
		this.run = run;
		this.report = report;
		initWidget(uiBinder.createAndBindUi(this));
		
		Anchor refresh = new Anchor("&#x021BB;", true);
		refresh.setStyleName(style.refresh());
		this.heading.entries.add(refresh);
		refresh.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Inspector.getInstance().generateReport(run, report, ReportPanel.this);
				refresh();
			}
		});
		
		refresh();
		
		addDomHandler(this, MouseDownEvent.getType());
	}

	public void refresh() {
		container.clear();
		Inspector.getInstance().getReportStatus(run, report, this);
	}
	
	@Override
	public void onMouseDown(MouseDownEvent event) {
		if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
			event.preventDefault(); // prevent anchors gaining focus on click
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		Entry entry = ((Entry.EntryPanel) event.getSource()).parent();
		if (entry.isOpen()) {
			entry.close();
		}
		else {
			if (!entry.isPopulated()) {
				Inspector.getInstance().getReportData(run, report, entry.getTarget(), entry, this);
			}
			entry.open();
		}
	}

	@Override
	public void statusUpdate(Status status) {
		progress.update(status);
		
		switch (status.state) {
		case UNINITIALIZED:
			Inspector.getInstance().generateReport(run, report, this);
			break;
		case GENERATING:
			timer.schedule(500);
			break;
		case READY:
			Inspector.getInstance().getReportData(run, report, null, null, this);
			break;
		}
	}

	@Override
	public void dataAvailable(Report.Entry key, Entry target, int available, ReportCallback<Entry> callback) {
		if (target == null) {
			callback.getData(key, target, 0, available);
		}
		else {
			target.setPopulated(true);
			callback.getData(key, target, 0, 50);
		}
	}

	@Override
	public void handleData(final Report.Entry key, final Entry target, final int offset, final int limit,
			int available, List<? extends Report.Entry> result, final ReportCallback<Entry> callback) {
		if (!isAttached() && isOrWasAttached()) return;
		
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
		
		if (offset + limit < available) {
			new Timer() {
				public void run() {
					callback.getData(key, target, offset + limit, limit);
				}
			}.schedule(200);
		}
	}

	@Override
	public void add(Entry entry) {
		container.add(entry);
		numEntries++;
		if (numEntries%2 == 0) {
			entry.addStyleName(style.even());
		}
	}
	
	@UiFactory
	Entry createHeading() {
		return new Entry(report);
	}
	
	private Timer timer = new Timer() {
		public void run() {
			Inspector.getInstance().getReportStatus(run, report, ReportPanel.this);
		}
	};
}
