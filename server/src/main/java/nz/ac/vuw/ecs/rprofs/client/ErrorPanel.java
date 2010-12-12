/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ErrorPanel extends Composite {

	private static ErrorPanel instance;
	
	static {
		instance = new ErrorPanel();
		RootPanel.get().add(instance);
	}
	
	private final Label message;
	private int version;
	
	public ErrorPanel() {
		version = 0;
		
		Panel wrapper = new FlowPanel();
		Style style = wrapper.getElement().getStyle();
		style.setPosition(Position.FIXED);
		style.setTop(0, Unit.PX);
		style.setWidth(100, Unit.PCT);
		style.setZIndex(100);
		style.setProperty("textAlign", "center");
		style.setDisplay(Display.NONE);
		message = new InlineLabel();
		wrapper.add(message);
		
		style = message.getElement().getStyle();
		style.setBackgroundColor("#c00");
		style.setColor("white");
		style.setProperty("padding", "0.2ex 2em");
		style.setProperty("borderBottomLeftRadius", "1em");
		style.setProperty("borderBottomRightRadius", "1em");
		
		initWidget(wrapper);
	}
	
	public static void showMessage(String message, Throwable error) {
		final int version = ++instance.version;
		instance.message.setText(message);
		instance.getElement().getStyle().setDisplay(Display.INLINE);
		new Timer() {
			@Override
			public void run() {
				clear(version);
			}
		}.schedule(5000);
		if (error != null) error.printStackTrace();
	}
	
	private static void clear(int version) {
		if (instance.version <= version) {
			instance.getElement().getStyle().setDisplay(Display.NONE);
		}
	}
}
