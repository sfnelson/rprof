package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.request.MethodProxy;

import java.util.Map;

public class EventCell extends AbstractCell<EventProxy> {

	private final EventStyle style;
	private final Map<InstanceProxy, Integer> threads;

	public EventCell(EventStyle style, Map<InstanceProxy, Integer> threads) {
		this.style = style;
		this.threads = threads;
	}

	@Override
	public void render(Context c, EventProxy e, SafeHtmlBuilder o) {
		if (e == null) return;

		o.appendHtmlConstant("<div class='" + style.eventCell() + " " + getStyleName(e) + "'>");

		renderThread(c, e, o);
		renderEvent(c, e, o);

		if ((e.getEvent() & EventProxy.METHODS) == e.getEvent()) {
			renderMethod(c, e, o);
		} else {
			renderType(c, e, o);
		}

		o.appendHtmlConstant("</div>");
	}

	void renderThread(Context c, EventProxy e, SafeHtmlBuilder o) {
		o.appendHtmlConstant("<span class='" + style.thread() + "' style='width: " + threads.size() + "ex'>");

		if (threads.containsKey(e.getThread())) {
			int position = threads.get(e.getThread());
			o.appendHtmlConstant("<span title='" + String.valueOf(e.getThread()) + "' style='margin-left: " + position + "ex'>");
			o.appendHtmlConstant("</span>");
		}

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
		} else {
			o.appendHtmlConstant("<span title=\"" + type.getName() + "\">");
			o.appendEscaped(type.getSimpleName());
			o.appendHtmlConstant("</span>");
		}

		o.appendHtmlConstant("</span>");
	}

	void renderMethod(Context c, EventProxy e, SafeHtmlBuilder o) {
		ClassProxy t = e.getType();
		MethodProxy m = e.getMethod();

		if (m.getName().equals("<init>")) {
			o.appendHtmlConstant("<span class='" + style.method() + "'>new</span> ");
			o.appendHtmlConstant("<span class='" + style.type() + "' title='" + t.getName() + "'>");
			o.appendEscaped(t.getSimpleName());
			o.appendHtmlConstant("</span>");
		} else {
			o.appendHtmlConstant("<span class='" + style.type() + "' title='" + t.getName() + "'>");
			o.appendEscaped(t.getSimpleName());
			o.appendHtmlConstant("</span>");
			o.appendHtmlConstant("<span class='" + style.method() + "'>.");
			o.appendEscaped(m.getName());
			o.appendHtmlConstant("</span>");
		}

		o.appendHtmlConstant("<span class='" + style.args() + "'>(");
		if (e.getArgs() != null) {
			for (InstanceProxy arg : e.getArgs()) {
				if (arg != null) {
					o.appendEscaped(arg.getType().getSimpleName());
				} else {
					o.appendHtmlConstant("null");
				}
			}
		}
		o.appendHtmlConstant(")</span>");
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
