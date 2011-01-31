package nz.ac.vuw.ecs.rprofs.client.ui;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.InspectorPlace;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;

public class PlaceAnchor<P extends Place> extends Composite implements HasText, HasHTML {

	private Anchor anchor;

	private final ProfilerFactory factory;
	private P target;

	public PlaceAnchor(ProfilerFactory factory) {
		this.factory = factory;

		anchor = new Anchor();
		initWidget(anchor);

		Handler handler = new Handler();
		anchor.addMouseOverHandler(handler);

		setStyleName("");
	}

	public void setTarget(P target) {
		this.target = target;
	}

	public P getTarget() {
		return target;
	}

	private void update() {
		if (target == null) {
			return;
		}

		InspectorPlace current = factory.getPlaceController().getCurrent();
		current = current.setPlace(target);
		anchor.setHref("#"+current.toString());
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
				factory.getPlaceController().goTo(target);
			}
			anchor.setFocus(false);
		}

	}
}
