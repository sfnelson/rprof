package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.safehtml.client.HasSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.client.place.PlaceBuilder;
import nz.ac.vuw.ecs.rprofs.client.place.ProfilerPlace;

public class PlaceAnchor extends Composite implements HasText, HasSafeHtml {

	private final PlaceHistoryMapper mapper;
	private final PlaceController pc;
	private final Anchor anchor;

	private PlaceBuilder builder;

	@Inject
	public PlaceAnchor(PlaceHistoryMapper mapper, PlaceController pc) {
		this.mapper = mapper;
		this.pc = pc;
		anchor = new Anchor();
		initWidget(anchor);

		Handler handler = new Handler();
		anchor.addMouseOverHandler(handler);
		anchor.addMouseDownHandler(handler);
		anchor.addClickHandler(handler);
		anchor.addFocusHandler(handler);

		setStyleName("");
	}

	public void setBuilder(PlaceBuilder builder) {
		this.builder = builder;
	}

	public ProfilerPlace getPlace() {
		return builder.get(pc.getWhere());
	}

	private void update() {
		if (builder == null) {
			anchor.setHref("#");
		}

		anchor.setHref("#" + mapper.getToken(getPlace()));
	}

	@Override
	public void setHTML(SafeHtml html) {
		anchor.setHTML(html);
	}

	@Override
	public String getText() {
		return anchor.getText();
	}

	@Override
	public void setText(String text) {
		anchor.setText(text);
	}

	private class Handler implements MouseOverHandler, MouseDownHandler, ClickHandler, FocusHandler {

		@Override
		public void onMouseOver(MouseOverEvent event) {
			update();
		}

		@Override
		public void onMouseDown(MouseDownEvent event) {
			event.preventDefault();
		}

		@Override
		public void onClick(ClickEvent event) {
			if (event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
				event.preventDefault();
				event.stopPropagation();
				pc.goTo(getPlace());
			}
			anchor.setFocus(false);
		}

		@Override
		public void onFocus(FocusEvent event) {
			update();
		}
	}
}
