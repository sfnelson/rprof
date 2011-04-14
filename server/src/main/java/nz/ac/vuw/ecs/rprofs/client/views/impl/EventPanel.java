package nz.ac.vuw.ecs.rprofs.client.views.impl;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Resources;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.client.ui.EventCell;
import nz.ac.vuw.ecs.rprofs.client.ui.EventStyle;
import nz.ac.vuw.ecs.rprofs.client.views.EventView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

public class EventPanel extends Composite implements EventView, ClickHandler {

	private class Pager extends AbstractPager implements ClickHandler {

		public Pager() {
			rewind.addClickHandler(this);
			previous.addClickHandler(this);
			next.addClickHandler(this);
			fastforward.addClickHandler(this);
		}

		@Override
		protected void onRangeOrRowCountChanged() {
			HasRows d = getDisplay();
			Range visible = d.getVisibleRange();

			if (visible.getStart() == 0) {
				disable(rewind);
				disable(previous);
				System.out.println("at start");
			}
			else {
				enable(rewind);
				enable(previous);
				System.out.println("not at start");
			}

			if (visible.getStart() + visible.getLength() + 1 >= d.getRowCount()) {
				disable(next);
				disable(fastforward);
				System.out.println("at end");
			}
			else {
				enable(next);
				enable(fastforward);
				System.out.println("not at end");
			}

			String txt = visible.getStart() + " to " + (visible.getStart() + visible.getLength()) + " of " + d.getRowCount();
			label.setText(txt);
		}

		private void disable(Anchor a) {
			a.removeStyleName(style.enabled());
		}

		private void enable(Anchor a) {
			a.addStyleName(style.enabled());
		}

		public void onClick(ClickEvent ev) {
			if (ev.getSource() == rewind) {

			}
			else if (ev.getSource() == previous) {

			}
			else if (ev.getSource() == next) {

			}
			else if (ev.getSource() == fastforward) {

			}
		}
	}

	private static EventReportPanelUiBinder uiBinder = GWT
	.create(EventReportPanelUiBinder.class);

	interface EventReportPanelUiBinder extends
	UiBinder<Widget, EventPanel> {
	}

	interface Style extends CssResource {
		String active();
		String enabled();
	}

	@UiField(provided=true) CellList<EventProxy> list;
	private AbstractPager pager;

	@UiField Style style;

	@UiField Anchor rewind;
	@UiField Anchor previous;
	@UiField HasHTML label;
	@UiField Anchor next;
	@UiField Anchor fastforward;

	@UiField Panel filterMenu;

	@UiField Anchor all;
	@UiField Anchor objectAllocated;
	@UiField Anchor arrayAllocated;
	@UiField Anchor methodEnter;
	@UiField Anchor methodReturn;
	@UiField Anchor methodException;
	@UiField Anchor fieldRead;
	@UiField Anchor fieldWrite;
	@UiField Anchor classWeave;
	@UiField Anchor classInit;
	@UiField Anchor objectTagged;
	@UiField Anchor objectFreed;

	private List<Anchor> filters = Collections.newList();

	private EventStyle eventStyle;
	private Presenter presenter;

	public EventPanel() {
		Resources res = GWT.create(Resources.class);
		eventStyle = res.eventStyle();
		list = new CellList<EventProxy>(new EventCell(eventStyle));

		initWidget(uiBinder.createAndBindUi(this));

		pager = new Pager();
		pager.setDisplay(list);

		init();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;

		presenter.getDataProvider().addDataDisplay(list);
		presenter.getAvailable();
	}

	@Override
	public void setAvailable(int events) {
		list.setRowCount(events);
	}

	@Override
	public void setFirst(int first) {
		list.setVisibleRange(first, 25);
	}

	@Override
	public void setFilter(int filter) {
		refresh(filter);
	}

	@Override
	public void onClick(ClickEvent ev) {
		if (ev.getSource() == all) {
			presenter.clearFilter();
		}
		else if (ev.getSource() instanceof Anchor) {
			int filter = getFilter((Anchor) ev.getSource());
			presenter.toggleFilter(filter);
		}
	}

	private void refresh(int filter) {
		if (filter == EventProxy.ALL) {
			all.addStyleName(style.active());
		}
		else {
			all.removeStyleName(style.active());
		}

		for (Anchor a: filters) {
			if ((getFilter(a) & filter) != 0) {
				a.addStyleName(style.active());
			}
			else {
				a.removeStyleName(style.active());
			}
		}
	}

	private void init() {
		all.addClickHandler(this);
		initButton(objectAllocated);
		initButton(arrayAllocated);
		initButton(methodEnter);
		initButton(methodReturn);
		initButton(methodException);
		initButton(fieldRead);
		initButton(fieldWrite);
		initButton(classWeave);
		initButton(classInit);
		initButton(objectTagged);
		initButton(objectFreed);
	}

	private void initButton(Anchor a) {
		a.addClickHandler(this);
		filters.add(a);
	}

	private int getFilter(Anchor a) {
		if (a == objectAllocated) {
			return EventProxy.OBJECT_ALLOCATED;
		}
		else if (a == arrayAllocated) {
			return EventProxy.ARRAY_ALLOCATED;
		}
		else if (a == methodEnter) {
			return EventProxy.METHOD_ENTER;
		}
		else if (a == methodReturn) {
			return EventProxy.METHOD_RETURN;
		}
		else if (a == methodException) {
			return EventProxy.METHOD_EXCEPTION;
		}
		else if (a == fieldRead) {
			return EventProxy.FIELD_READ;
		}
		else if (a == fieldWrite) {
			return EventProxy.FIELD_WRITE;
		}
		else if (a == classWeave) {
			return EventProxy.CLASS_WEAVE;
		}
		else if (a == classInit) {
			return EventProxy.CLASS_INITIALIZED;
		}
		else if (a == objectTagged) {
			return EventProxy.OBJECT_TAGGED;
		}
		else if (a == objectFreed) {
			return EventProxy.OBJECT_FREED;
		}
		else {
			return 0;
		}
	}
}
