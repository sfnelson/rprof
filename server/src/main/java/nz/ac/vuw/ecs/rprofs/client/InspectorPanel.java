package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.events.ReportListHandler;
import nz.ac.vuw.ecs.rprofs.client.history.History;
import nz.ac.vuw.ecs.rprofs.client.history.HistoryManager;
import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
import nz.ac.vuw.ecs.rprofs.client.ui.UIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class InspectorPanel extends Composite implements ReportListHandler, ValueChangeHandler<History> {

	private static InspectorPanelUiBinder uiBinder = GWT
	.create(InspectorPanelUiBinder.class);

	interface InspectorPanelUiBinder extends UiBinder<Widget, InspectorPanel> {
	}

	@UiField Style style;

	interface Style extends CssResource {
		String button();
		String selected();
	}

	@UiField Panel panel;
	@UiField Panel menu;
	@UiField Panel content;

	@UiField UIButton close;

	private final FrameLayout parent;

	private boolean closed = false;

	private History previous;
	private Map<String, View> views = Collections.newMap();
	private Map<String, Anchor> buttons = Collections.newMap();

	public InspectorPanel(FrameLayout parent) {
		this.parent = parent;

		initWidget(uiBinder.createAndBindUi(this));

		Inspector.getInstance().addReportListHandler(this);
		HistoryManager.getInstance().addValueChangeHandler(this);
	}

	public void addView(final View view) {
		final Anchor a = new Anchor(view.getTitle());
		a.setHref("#view=" + view.getIdentifier());
		a.setTitle(view.getDescription());
		a.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				History history = HistoryManager.getInstance().getHistory();
				history.view = view.getIdentifier();
				a.setHref("#"+history.toString());
			}
		});
		a.addMouseDownHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				event.preventDefault();
			}
		});
		a.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
					event.preventDefault();
					event.stopPropagation();

					History history = HistoryManager.getInstance().getHistory();
					history.view = view.getIdentifier();
					HistoryManager.getInstance().update(history);
					a.setFocus(false);
				}
			}
		});
		a.setStyleName(style.button());
		menu.add(a);
		views.put(view.getIdentifier(), view);
		buttons.put(view.getIdentifier(), a);
	}

	@Override
	public void onValueChange(ValueChangeEvent<History> event) {
		update(event.getValue());
	}

	private void update(History current) {
		if (previous == null);
		else if (current == null);
		else if (previous.view == null);
		else if (previous.run == null);
		else if (previous.view.equals(current.view) && previous.run.equals(current.run)) {
			previous = current;
			return;
		}

		content.clear();
		if (previous != null && buttons.containsKey(previous.view)) {
			buttons.get(previous.view).removeStyleName(style.selected());
		}

		if (current == null || current.run == null || current.view == null) {
			previous = current;
			return;
		}

		if (buttons.get(current.view) != null) {
			buttons.get(current.view).addStyleName(style.selected());
			content.add(views.get(current.view).createWidget(current));
		}

		previous = current;
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

	@Override
	public void onReportsAvailable(List<Report> reports) {
		menu.clear();
		for (Report report: reports) {
			addView(new ReportView(report));
		}
		addView(new LogView());
		previous = null;
		update(HistoryManager.getInstance().getHistory());
	}
}
