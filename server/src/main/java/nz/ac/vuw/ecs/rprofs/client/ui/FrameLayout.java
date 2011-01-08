/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class FrameLayout extends Composite {

	public static final int MAX_HEIGHT = 0x1;
	public static final int HIDE_TOP = 0x2;
	public static final int HIDE_BOTTOM = 0x4;

	private static FrameLayoutUiBinder uiBinder = GWT
	.create(FrameLayoutUiBinder.class);

	interface FrameLayoutUiBinder extends UiBinder<Widget, FrameLayout> {
	}

	@UiField Style style;

	interface Style extends CssResource {
		String hideTop();
		String hideBottom();
	}

	@UiField Panel wrapper;

	@UiField BorderPanel top;
	@UiField Spacer topSpacer;
	@UiField Panel center;
	@UiField Spacer bottomSpacer;
	@UiField BorderPanel bottom;

	public FrameLayout() {
		this(0);
	}

	public FrameLayout(int flags) {
		this(flags, 20, 20, Unit.EX);
	}

	public FrameLayout(int flags, double topHeight, double bottomHeight, Unit unit) {
		initWidget(uiBinder.createAndBindUi(this));

		topSpacer.panel = this.top;
		bottomSpacer.panel = this.bottom;

		this.top.setHeight(topHeight, unit);
		this.bottom.setHeight(bottomHeight, unit);
		this.wrapper.getElement().getStyle().setProperty("minHeight", (topHeight+bottomHeight)*1.25, unit);

		if ((flags & MAX_HEIGHT) == MAX_HEIGHT) {
			setHeight("100%");
		}

		showTop((flags & HIDE_TOP) != HIDE_TOP);
		showBottom((flags & HIDE_BOTTOM) != HIDE_BOTTOM);

		Event.addNativePreviewHandler(new NativePreviewHandler() {
			@Override
			public void onPreviewNativeEvent(NativePreviewEvent event) {
				if (dragging == null) return;

				if (event.getTypeInt() == Event.ONMOUSEUP) {
					dragging = null;
				}
			}
		});
	}

	public void setTop(IsWidget top) {
		this.top.clear();
		if (top == null) {
			showTop(false);
		}
		else {
			this.top.add(top);
			showTop(true);
		}
	}

	public void setCenter(IsWidget center) {
		this.center.clear();
		this.center.add(center);
	}

	public void setBottom(IsWidget bottom) {
		this.bottom.clear();
		if (bottom == null) {
			showBottom(false);
		}
		else {
			this.bottom.add(bottom);
			showBottom(true);
		}
	}

	public void showTop(boolean show) {
		if (show) {
			top.setVisible(true);
			topSpacer.setVisible(true);

			if (top.unit == Unit.PX) {
				int limit = bottomSpacer.getElement().getOffsetTop() - Spacer.HEIGHT;
				if (top.height < Spacer.SNAP) top.height = 0;
				else if (bottom.isVisible() && top.height + Spacer.SNAP > limit) top.height = limit;
			}

			top.getElement().getStyle().setHeight(top.height, top.unit);
			topSpacer.getElement().getStyle().setTop(top.height, top.unit);
			center.getElement().getStyle().setTop(top.height, top.unit);
			removeStyleName(style.hideTop());
		}
		else {
			top.setVisible(false);
			topSpacer.setVisible(false);
			center.getElement().getStyle().setTop(0, Unit.PX);
			addStyleName(style.hideTop());
		}
	}

	public void showBottom(boolean show) {
		if (show) {
			bottom.setVisible(true);
			bottomSpacer.setVisible(true);

			if (bottom.unit == Unit.PX) {
				int limit = wrapper.getElement().getOffsetHeight() - topSpacer.getElement().getOffsetTop() - Spacer.HEIGHT * 2;
				if (bottom.height < Spacer.SNAP) bottom.height = 0;
				else if (top.isVisible() && bottom.height + Spacer.SNAP > limit) bottom.height = limit;
			}

			bottom.getElement().getStyle().setHeight(bottom.height, bottom.unit);
			bottomSpacer.getElement().getStyle().setBottom(bottom.height, bottom.unit);
			center.getElement().getStyle().setBottom(bottom.height, bottom.unit);
			removeStyleName(style.hideBottom());
		}
		else {
			bottom.setVisible(false);
			bottomSpacer.setVisible(false);
			center.getElement().getStyle().setBottom(0, Unit.PX);
			addStyleName(style.hideBottom());
		}
	}

	private Spacer dragging;

	@UiHandler("topSpacer")
	void mouseDownTop(MouseDownEvent event) {
		dragging = topSpacer;
		dragging.lastPosition = top.height;
		dragging.lastUnit = top.unit;
		event.stopPropagation();
		event.preventDefault();
	}

	@UiHandler("bottomSpacer")
	void mouseDownBottom(MouseDownEvent event) {
		dragging = bottomSpacer;
		dragging.lastPosition = bottom.height;
		dragging.lastUnit = bottom.unit;
		event.stopPropagation();
		event.preventDefault();
	}

	@UiHandler("wrapper")
	void mouseDragged(MouseMoveEvent event) {
		if (dragging == null) return;

		if (dragging == topSpacer) {
			int height = event.getRelativeY(wrapper.getElement());
			top.setHeight(height, Unit.PX);
			showTop(true);
		}
		else {
			int height = wrapper.getElement().getClientHeight() - event.getRelativeY(wrapper.getElement());
			bottom.setHeight(height, Unit.PX);
			showBottom(true);
		}
	}

	@UiHandler("wrapper")
	void mouseUp(MouseUpEvent event) {
		if (dragging == null) return;
		dragging = null;
	}

	@UiHandler("wrapper")
	void mouseOut(MouseOutEvent event) {
		if (dragging == null) return;

		dragging.panel.setHeight(dragging.lastPosition, dragging.lastUnit);
		if (dragging == topSpacer) {
			showTop(true);
		}
		else {
			showBottom(true);
		}
	}

	static class BorderPanel extends FlowPanel {
		double height;
		Unit unit;

		public BorderPanel() {
			height = 0;
			unit = Unit.PX;
		}

		public void setHeight(double height, Unit unit) {
			this.height = height;
			this.unit = unit;
		}
	}

	static class Spacer extends ActivePanel {
		public static final int SNAP = 20;
		public static final int HEIGHT = 2;

		double lastPosition;
		Unit lastUnit;
		BorderPanel panel;
	}
}
