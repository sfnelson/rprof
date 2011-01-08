/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class UIButton extends Composite implements HasClickHandlers, HasHTML {

	private static UIButtonUiBinder uiBinder = GWT
	.create(UIButtonUiBinder.class);

	interface UIButtonUiBinder extends UiBinder<Widget, UIButton> {
	}

	interface Style extends CssResource {
		String depressed();
	}

	@UiField Style style;
	@UiField Anchor button;

	public UIButton() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("button")
	void onMouseDown(MouseDownEvent e) {
		e.preventDefault();
		e.stopPropagation();
		if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
			button.addStyleName(style.depressed());
		}
	}

	@UiHandler("button")
	void onMouseUp(ClickEvent e) {
		e.preventDefault();
		e.stopPropagation();
		if (e.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
			button.removeStyleName(style.depressed());
		}
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return button.addClickHandler(handler);
	}

	@Override
	public String getHTML() {
		return button.getHTML();
	}

	@Override
	public void setHTML(String html) {
		button.setHTML(html);
	}

	@Override
	public String getText() {
		return button.getText();
	}

	@Override
	public void setText(String text) {
		button.setHTML(text);
	}
}
