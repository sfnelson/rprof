package nz.ac.vuw.ecs.rprofs.client.views.impl;

import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class DatasetPanel extends Composite implements DatasetListView {

	private static DatasetPanelUiBinder uiBinder = GWT.create(DatasetPanelUiBinder.class);

	interface DatasetPanelUiBinder extends UiBinder<Widget, DatasetPanel> {}

	@UiField Style style;

	interface Style extends CssResource {
		String selected();
	}

	@UiField FlowPanel panel;

	private List<DatasetWidget> available = Collections.newList();
	private DatasetWidget selected;
	private Map<DatasetProxy, DatasetWidget> viewMap = Collections.newMap();

	public DatasetPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void addDataset(DatasetProxy dataset, DatasetView.Presenter presenter) {
		DatasetWidget w;
		if (available.isEmpty()) {
			w = new DatasetWidget();
		}
		else {
			w = available.remove(available.size() - 1);
			panel.remove(w);
		}

		w.setDataset(dataset);
		w.setPresenter(presenter);
		viewMap.put(dataset, w);

		panel.add(w);
		w.setVisible(true);
	}

	@Override
	public void updateDataset(DatasetProxy dataset) {
		DatasetWidget view = viewMap.get(dataset);

		view.setDataset(dataset);
	}

	@Override
	public void selectDataset(DatasetProxy dataset) {
		DatasetWidget view = viewMap.get(dataset);

		if (selected != null) {
			selected.removeStyleName(style.selected());
		}

		selected = view;

		if (selected != null) {
			selected.addStyleName(style.selected());
		}
	}

	@Override
	public void removeDataset(DatasetProxy dataset) {
		DatasetWidget view = viewMap.get(dataset);

		if (selected == view) {
			selected.removeStyleName(style.selected());
			selected = null;
		}

		view.setVisible(false);
		available.add(view);
		viewMap.remove(dataset);
	}

}
