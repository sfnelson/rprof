package nz.ac.vuw.ecs.rprofs.client.views.impl;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ReportWidget extends Composite {

	private static ReportWidgetUiBinder uiBinder = GWT.create(ReportWidgetUiBinder.class);
	interface ReportWidgetUiBinder extends UiBinder<Widget, ReportWidget> {}
	interface Style extends CssResource {
		String even();
		String refresh();
	}

	@UiField Label name;
	@UiField HasWidgets children;

	private ReportPanel parent;
	private List<ReportWidget> widgets;

	public ReportWidget(ReportPanel parent) {
		this.parent = parent;
		widgets = Collections.newList();

		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setName(String name) {
		this.name.setText(name);
	}

	public void addChild(ReportWidget child) {
		children.add(child);
	}

	public List<ReportWidget> remove() {
		List<ReportWidget> widgets = this.widgets;
		this.widgets = Collections.newList();
		this.children.clear();
		return widgets;
	}

	@UiHandler("name")
	public void onClick(ClickEvent ev) {
		parent.select(this);
	}
}
