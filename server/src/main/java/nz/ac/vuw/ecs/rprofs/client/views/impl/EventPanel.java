package nz.ac.vuw.ecs.rprofs.client.views.impl;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.client.views.EventView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class EventPanel extends Composite implements EventView {

	private static EventPanelUiBinder uiBinder = GWT
	.create(EventPanelUiBinder.class);

	interface EventPanelUiBinder extends UiBinder<Widget, EventPanel> {}

	private List<EventWidget> contents = Collections.newList();
	private List<EventWidget> available = Collections.newList();

	@UiField HasHTML title;
	@UiField HasWidgets children;

	@SuppressWarnings("unused")
	private Presenter presenter;

	public EventPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setTitle(String title) {
		this.title.setHTML(title);
	}

	@Override
	public void addEvent(EventProxy event) {
		EventWidget w = createWidget();
		w.setEvent(event);
		children.add(w);
		contents.add(w);
	}

	@Override
	public void clear() {
		for (EventWidget w: contents) {
			children.remove(w);
			available.add(w);
		}
	}

	private EventWidget createWidget() {
		if (available.isEmpty()) {
			return new EventWidget();
		}
		else {
			return available.remove(available.size() - 1);
		}
	}
}
