package nz.ac.vuw.ecs.rprofs.client.views.impl;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import nz.ac.vuw.ecs.rprofs.client.place.HasView;
import nz.ac.vuw.ecs.rprofs.client.place.PlaceBuilder;
import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
import nz.ac.vuw.ecs.rprofs.client.ui.PlaceAnchor;
import nz.ac.vuw.ecs.rprofs.client.ui.UIButton;
import nz.ac.vuw.ecs.rprofs.client.views.ReportSelectorView;

import java.util.Map;

public class ReportSelectionPanel extends Composite implements ReportSelectorView {

	private static ReportSelectionPanelUiBinder uiBinder = GWT.create(ReportSelectionPanelUiBinder.class);

	interface ReportSelectionPanelUiBinder extends UiBinder<Widget, ReportSelectionPanel> {
	}

	interface Style extends CssResource {
		String selected();

		String menu();

		String content();

		String menuWrapper();

		String closed();

		String button();

		String closeButton();
	}

	@UiField
	Style style;

	@UiField
	Panel panel;
	@UiField
	Panel menu;
	@UiField
	SimplePanel content;

	@UiField
	UIButton close;

	private final Provider<FrameLayout> parent;
	private final PlaceController pc;
	private final PlaceHistoryMapper mapper;

	private final Map<String, PlaceAnchor> buttons = Maps.newHashMap();

	private PlaceAnchor selected;

	private boolean closed = false;

	@Inject
	public ReportSelectionPanel(PlaceController pc, EventBus bus, PlaceHistoryMapper mapper,
								Provider<FrameLayout> parent) {
		this.parent = parent;
		this.pc = pc;
		this.mapper = mapper;

		initWidget(uiBinder.createAndBindUi(this));

		bus.addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {
			@Override
			public void onPlaceChange(PlaceChangeEvent event) {
				if (event.getNewPlace() instanceof HasView) {
					HasView place = (HasView) event.getNewPlace();
					setSelected(place.getView());
				}
			}
		});
	}

	@Override
	public void setSelected(String report) {
		if (selected != null) {
			selected.removeStyleName(style.selected());
		}

		selected = buttons.get(report);

		if (selected != null) {
			selected.addStyleName(style.selected());
		}
	}

	@UiHandler("close")
	void closeClicked(ClickEvent e) {
		if (!closed) {
			parent.get().showTop(false);
			close.setHTML("<span>&raquo;</span>");
		} else {
			parent.get().showTop(true);
			close.setHTML("<span>&laquo;</span>");
		}
		closed = !closed;
	}

	@UiFactory
	PlaceAnchor createAnchor(String view, String title, String description) {
		PlaceAnchor anchor = new PlaceAnchor(mapper, pc);

		anchor.setBuilder(PlaceBuilder.create().setView(view));

		anchor.setText(title);
		anchor.setTitle(description);

		buttons.put(view, anchor);

		return anchor;
	}

	@Override
	public void setWidget(IsWidget w) {
		content.setWidget(w);
	}
}
