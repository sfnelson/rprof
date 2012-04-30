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
import com.google.gwt.user.client.ui.*;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class SplitLayout extends Composite {

	public static final int MAX_HEIGHT = 1;
	public static final int HIDE_TOP = 2;
	public static final int HIDE_BOTTOM = 4;

	private static FrameLayoutUiBinder uiBinder = GWT
			.create(FrameLayoutUiBinder.class);

	interface FrameLayoutUiBinder extends UiBinder<Widget, SplitLayout> {
	}

	@UiField
	Style style;

	interface Style extends CssResource {
		String hideBottom();

		String hideTop();

		String bottom();
	}

	@UiField
	ActivePanel wrapper;

	@UiField
	BorderPanel top;

	@UiField
	Spacer spacer;

	@UiField
	BorderPanel bottom;

	public SplitLayout() {
		this(0);
	}

	public SplitLayout(int flags) {
		this(flags, 20, 20, Unit.EX);
	}

	public SplitLayout(int flags, double topHeight, double bottomHeight, Unit unit) {
		initWidget(uiBinder.createAndBindUi(this));

		spacer.panel = this.top;

		this.top.setHeight(topHeight, unit);
		this.wrapper.getElement().getStyle().setProperty("minHeight", (topHeight + bottomHeight) * 1.25, unit);

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

	public AcceptsOneWidget getTop() {
		return new AcceptsOneWidget() {
			@Override
			public void setWidget(IsWidget w) {
				top.clear();
				if (w == null) {
					showTop(false);
				} else {
					top.add(w);
					showTop(true);
				}
			}
		};
	}

	public AcceptsOneWidget getBottom() {
		return new AcceptsOneWidget() {
			@Override
			public void setWidget(IsWidget w) {
				bottom.clear();
				if (w == null) {
					showBottom(false);
				} else {
					bottom.add(w);
					showBottom(true);
				}
			}
		};
	}

	public void showTop(boolean show) {
		if (show) {
			top.setVisible(true);
			spacer.setVisible(true);

			if (top.unit == Unit.PX) {
				int limit = 0;
				if (top.height < Spacer.SNAP) top.height = 0;
				else if (bottom.isVisible() && top.height + Spacer.SNAP > limit) top.height = limit;
			}

			top.getElement().getStyle().setHeight(top.height, top.unit);
			spacer.getElement().getStyle().setTop(top.height, top.unit);
			bottom.getElement().getStyle().setTop(top.height, top.unit);
			removeStyleName(style.hideTop());
		} else {
			top.setVisible(false);
			spacer.setVisible(false);
			bottom.getElement().getStyle().setTop(0, Unit.PX);
			addStyleName(style.hideTop());
		}
	}

	public void showBottom(boolean show) {
		if (show) {
			bottom.setVisible(true);
		}
	}

	private Spacer dragging;

	@UiHandler("spacer")
	void mouseDownTop(MouseDownEvent event) {
		dragging = spacer;
		dragging.lastPosition = top.height;
		dragging.lastUnit = top.unit;
		event.stopPropagation();
		event.preventDefault();
	}

	@UiHandler("wrapper")
	void mouseDragged(MouseMoveEvent event) {
		if (dragging == spacer) {
			int height = event.getRelativeY(wrapper.getElement());
			top.setHeight(height, Unit.PX);
			showTop(true);
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
		if (dragging == spacer) {
			showTop(true);
		} else {
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
