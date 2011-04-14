package nz.ac.vuw.ecs.rprofs.client.ui;

import nz.ac.vuw.ecs.rprofs.client.place.shared.PlaceBuilder;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;

public class PlaceAnchor<P extends Place> extends Composite implements HasText, HasHTML {

	private final PlaceController pc;
	private final Anchor anchor;

	private PlaceBuilder<P> target;

	public PlaceAnchor(PlaceController pc) {
		this.pc = pc;
		anchor = new Anchor();
		initWidget(anchor);

		Handler handler = new Handler();
		anchor.addMouseOverHandler(handler);

		setStyleName("");
	}

	public void setTarget(PlaceBuilder<P> target) {
		this.target = target;
	}

	public P getTarget() {
		return target.getPlace();
	}

	private void update() {
		if (target == null) {
			anchor.setHref("#");
		}

		anchor.setHref("#" + target.getPlace());
	}

	@Override
	public String getHTML() {
		return anchor.getHTML();
	}

	@Override
	public void setHTML(String html) {
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

	private class Handler implements MouseOverHandler, MouseDownHandler, ClickHandler {

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
				pc.goTo(target.getPlace());
			}
			anchor.setFocus(false);
		}

	}
}
