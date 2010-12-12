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
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
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
	
	private HandlerManager manager;
	
	public UIButton() {
		initWidget(uiBinder.createAndBindUi(this));
		
		Event.addNativePreviewHandler(new NativePreviewHandler() {
			@Override
			public void onPreviewNativeEvent(NativePreviewEvent event) {
				if (event.getTypeInt() == Event.ONMOUSEUP) {
					button.removeStyleName(style.depressed());
				}
			}
		});
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
	void onClick(ClickEvent e) {
		e.preventDefault();
		e.stopPropagation();
		if (e.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT && manager != null) {
			manager.fireEvent(e);
		}
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		if (manager == null) {
			manager = new HandlerManager(this);
		}
		return manager.addHandler(ClickEvent.getType(), handler);
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
