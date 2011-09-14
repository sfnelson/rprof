package nz.ac.vuw.ecs.rprofs.client.views.impl;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import nz.ac.vuw.ecs.rprofs.client.place.shared.PlaceBuilder;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlace;
import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
import nz.ac.vuw.ecs.rprofs.client.ui.PlaceAnchor;
import nz.ac.vuw.ecs.rprofs.client.ui.UIButton;
import nz.ac.vuw.ecs.rprofs.client.views.ReportSelectorView;

import java.util.Map;

public class ReportSelectionPanel extends Composite implements ReportSelectorView {

	private static ReportSelectionPanelUiBinder uiBinder = GWT.create(ReportSelectionPanelUiBinder.class);

	interface ReportSelectionPanelUiBinder extends UiBinder<Widget, ReportSelectionPanel> {
	}

	@UiField
	Style style;

	interface Style extends CssResource {
		String button();

		String selected();

		String hidden();
	}

	@UiField
	Panel panel;
	@UiField
	Panel menu;
	@UiField
	SimplePanel content;

	@UiField
	UIButton close;

	private final FrameLayout parent;
	private final PlaceController pc;

	private final Map<String, PlaceAnchor<ReportPlace<?>>> buttons = Maps.newHashMap();

	private PlaceAnchor<ReportPlace<?>> selected;

	private boolean closed = false;

	public ReportSelectionPanel(PlaceController pc, EventBus bus, FrameLayout parent) {
		this.parent = parent;
		this.pc = pc;

		initWidget(uiBinder.createAndBindUi(this));

		bus.addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {
			@Override
			public void onPlaceChange(PlaceChangeEvent event) {
				if (event.getNewPlace() instanceof ReportPlace) {
					setSelected(((ReportPlace<?>) event.getNewPlace()).getType());
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
			parent.showTop(false);
			close.setHTML("<span>&raquo;</span>");
		} else {
			parent.showTop(true);
			close.setHTML("<span>&laquo;</span>");
		}
		closed = !closed;
	}

	@UiFactory
	PlaceAnchor<ReportPlace<?>> createAnchor(String reference, String title, String description) {
		PlaceAnchor<ReportPlace<?>> anchor = new PlaceAnchor<ReportPlace<?>>(pc);

		final String report = reference;
		anchor.setTarget(new PlaceBuilder<ReportPlace<?>>() {
			@Override
			public ReportPlace<?> getPlace() {
				return ReportPlace.create(report, pc.getWhere());
			}
		});

		anchor.setText(title);
		anchor.setTitle(description);

		buttons.put(reference, anchor);

		return anchor;
	}

	@Override
	public void setWidget(IsWidget w) {
		content.setWidget(w);
	}
}
