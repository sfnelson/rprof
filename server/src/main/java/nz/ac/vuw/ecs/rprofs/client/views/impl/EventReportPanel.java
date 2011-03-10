package nz.ac.vuw.ecs.rprofs.client.views.impl;

import nz.ac.vuw.ecs.rprofs.client.Resources;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.ui.EventCell;
import nz.ac.vuw.ecs.rprofs.client.ui.EventStyle;
import nz.ac.vuw.ecs.rprofs.client.views.EventReportView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EventReportPanel extends Composite implements EventReportView {

	private static EventReportPanelUiBinder uiBinder = GWT
	.create(EventReportPanelUiBinder.class);

	interface EventReportPanelUiBinder extends
	UiBinder<Widget, EventReportPanel> {
	}

	@UiField(provided=true) EventStyle style;
	@UiField(provided=true) CellList<EventProxy> list;

	private Presenter presenter;

	public EventReportPanel() {
		Resources res = GWT.create(Resources.class);
		style = res.eventStyle();
		list = new CellList<EventProxy>(new EventCell(style));

		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;

		presenter.getDataProvider().addDataDisplay(list);
	}

}
