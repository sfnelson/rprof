package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.HandlerRegistration;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class PlaceAnchor extends Composite {

	private final Anchor anchor;

	private Provider<String> url;

	public PlaceAnchor(@NotNull Provider<String> url, @NotNull SafeHtml text, @Nullable String title) {
		anchor = new Anchor();
		initWidget(anchor);

		Handler handler = new Handler();
		anchor.addMouseOverHandler(handler);
		anchor.addFocusHandler(handler);

		setStyleName("");

		anchor.setHTML(text);
		anchor.setHref(url.get());
		if (title != null) anchor.setTitle(title);

		this.url = url;
	}

	private void update() {
		anchor.setHref("#" + url.get());
	}

	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return anchor.addClickHandler(handler);
	}

	private class Handler implements MouseOverHandler, FocusHandler {

		@Override
		public void onMouseOver(MouseOverEvent event) {
			update();
		}

		@Override
		public void onFocus(FocusEvent event) {
			update();
		}
	}
}
