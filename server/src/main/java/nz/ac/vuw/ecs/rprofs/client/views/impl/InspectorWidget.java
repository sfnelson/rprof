package nz.ac.vuw.ecs.rprofs.client.views.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.client.views.ProfilerAppView;
import nz.ac.vuw.ecs.rprofs.client.views.ViewListView;

public class InspectorWidget extends Composite implements ProfilerAppView {

	interface Binder extends UiBinder<Widget, InspectorWidget> {
	}

	@UiField
	SplitLayoutPanel panel;

	@UiField
	SimpleLayoutPanel top;

	@UiField(provided = true)
	ViewListView bottom;

	@Inject
	public InspectorWidget(ViewListView views) {
		this.bottom = views;

		initWidget(GWT.<Binder>create(Binder.class).createAndBindUi(this));
	}

	@Override
	public AcceptsOneWidget getDatasetContainer() {
		return top;
	}

	@Override
	public AcceptsOneWidget getInspectorContainer() {
		return bottom;
	}

	@UiFactory
	SplitLayoutPanel createSplitterPanel() {
		return new SplitLayoutPanel(2);
	}
}
