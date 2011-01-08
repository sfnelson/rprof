package nz.ac.vuw.ecs.rprofs.client.views.impl;

import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.requests.ReportProxy;
import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
import nz.ac.vuw.ecs.rprofs.client.ui.UIButton;
import nz.ac.vuw.ecs.rprofs.client.views.ReportManagerView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ReportManagerPanel extends Composite implements ReportManagerView {

	private static ReportPanelUiBinder uiBinder = GWT.create(ReportPanelUiBinder.class);

	interface ReportPanelUiBinder extends UiBinder<Widget, ReportManagerPanel> {
	}

	@UiField Style style;

	interface Style extends CssResource {
		String button();
		String selected();
		String hidden();
	}

	@UiField Panel panel;
	@UiField Panel menu;
	@UiField SimplePanel content;

	@UiField UIButton close;

	private final FrameLayout parent;

	private Presenter presenter;

	private boolean closed = false;

	private Map<ReportProxy, ReportButton> reports = Collections.newMap();
	private List<ReportButton> available = Collections.newList();
	private ReportButton selected;

	public ReportManagerPanel(FrameLayout parent) {
		this.parent = parent;

		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	public void addReport(ReportProxy report) {
		ReportButton b;
		if (available.isEmpty()) {
			b = new ReportButton();
		}
		else {
			b = available.remove(available.size() - 1);
			b.removeStyleName(style.hidden());
			b.removeFromParent();
		}

		reports.put(report, b);
		b.setReport(report);
		menu.add(b);
	}

	public void updateReport(ReportProxy report) {
		if (!reports.containsKey(report)) return;

		ReportButton b = reports.get(report);
		b.setReport(report);
	}

	public void selectReport(ReportProxy report) {
		if (!reports.containsKey(report)) return;

		if (selected != null) {
			selected.removeStyleName(style.selected());
		}

		selected = reports.get(report);

		if (selected != null) {
			selected.addStyleName(style.selected());
		}
	}

	public void removeReport(ReportProxy report) {
		if (!reports.containsKey(report)) return;

		ReportButton b = reports.remove(report);
		if (selected == b) {
			b.removeStyleName(style.selected());
		}
		b.addStyleName(style.hidden());
		available.add(b);
	}

	@Override
	public AcceptsOneWidget getReportContainer() {
		return content;
	}

	@UiHandler("close")
	void closeClicked(ClickEvent e) {
		if (!closed) {
			parent.showTop(false);
			close.setHTML("<span>&raquo;</span>");
		} else {
			parent.showTop(true);
			close.setHTML("<span>&laquo;</span>");
		}
		closed = !closed;
	}

	private class ReportButton extends Anchor implements MouseOverHandler, MouseDownHandler, ClickHandler {

		private ReportProxy report;

		public ReportButton() {
			addMouseOverHandler(this);
			addMouseDownHandler(this);
			addClickHandler(this);

			setStyleName(style.button());
		}

		public void setReport(ReportProxy report) {
			this.report = report;
			this.setText(report.getTitle());
			this.setHref(presenter.updateLink(report));
			this.setTitle(report.getDescription());
		}

		@Override
		public void onClick(ClickEvent event) {
			if (event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
				event.preventDefault();
				event.stopPropagation();
				presenter.selectReport(report);
			}
			setFocus(false);
		}

		@Override
		public void onMouseDown(MouseDownEvent event) {
			event.preventDefault();
		}

		@Override
		public void onMouseOver(MouseOverEvent event) {
			setHref(presenter.updateLink(report));
		}
	}
}
