package nz.ac.vuw.ecs.rprofs.client.ui;

import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class EventCell extends AbstractCell<EventProxy> {

	private final EventStyle style;

	public EventCell(EventStyle style) {
		this.style = style;
	}

	@Override
	public void render(Context c, EventProxy e, SafeHtmlBuilder o) {
		o.appendHtmlConstant("<div class='" + getStyleName(e) + "'>");

		renderThread(c, e, o);
		renderEvent(c, e, o);
		renderType(c, e, o);

		o.appendHtmlConstant("</div>");
	}

	void renderThread(Context c, EventProxy e, SafeHtmlBuilder o) {
		o.appendHtmlConstant("<span class='" + style.thread() + "'>");

		o.appendEscaped(String.valueOf(e.getThread()));

		o.appendHtmlConstant("</span>");
	}

	void renderEvent(Context c, EventProxy e, SafeHtmlBuilder o) {
		o.appendHtmlConstant("<span class='" + style.event() + "'>");

		o.appendHtmlConstant(getEventDescription(e));

		o.appendHtmlConstant("</span>");
	}

	void renderType(Context c, EventProxy e, SafeHtmlBuilder o) {
		o.appendHtmlConstant("<span class='" + style.type() + "'>");

		ClassProxy type = e.getType();

		if (type == null) {
			o.appendHtmlConstant("<em>null</em>");
		}
		else {
			o.appendHtmlConstant("<span title=\"" + type.getName() + "\">");
			o.appendEscaped(type.getClassName());
			o.appendHtmlConstant("</span>");
		}

		o.appendHtmlConstant("</span>");
	}

	String getEventDescription(EventProxy e) {
		switch (e.getEvent()) {
		case EventProxy.OBJECT_ALLOCATED:
			return "new";
		case EventProxy.ARRAY_ALLOCATED:
			return "new[]";
		case EventProxy.METHOD_ENTER:
			return "&rarr;";
		case EventProxy.METHOD_RETURN:
			return "&larr;";
		case EventProxy.FIELD_READ:
			return "<em>read</em>";
		case EventProxy.FIELD_WRITE:
			return "<em>write</em>";
		case EventProxy.CLASS_WEAVE:
			return "<em>weave</em>";
		case EventProxy.CLASS_INITIALIZED:
			return "<em>init</em>";
		case EventProxy.OBJECT_TAGGED:
			return "<em>tag</em>";
		case EventProxy.OBJECT_FREED:
			return "<em>free</em>";
		case EventProxy.METHOD_EXCEPTION:
			return "&#x219A;";
		default:
			return "<em>unknown</em>";
		}
	}

	String getStyleName(EventProxy e) {
		switch (e.getEvent()) {
		case EventProxy.OBJECT_ALLOCATED:
			return style.objectAllocated();
		case EventProxy.ARRAY_ALLOCATED:
			return style.arrayAllocated();
		case EventProxy.METHOD_ENTER:
			return style.methodEnter();
		case EventProxy.METHOD_RETURN:
			return style.methodExit();
		case EventProxy.FIELD_READ:
			return style.fieldRead();
		case EventProxy.FIELD_WRITE:
			return style.fieldWrite();
		case EventProxy.CLASS_WEAVE:
			return style.classWeave();
		case EventProxy.CLASS_INITIALIZED:
			return style.classInit();
		case EventProxy.OBJECT_TAGGED:
			return style.objectTagged();
		case EventProxy.OBJECT_FREED:
			return style.objectFreed();
		case EventProxy.METHOD_EXCEPTION:
			return style.methodException();
		default:
			return "";
		}
	}
}
