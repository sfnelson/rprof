package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.ClazzIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.InstanceIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.MethodIdProxy;

import java.util.Map;

public class EventCell extends AbstractCell<EventProxy> {

	interface Templates extends SafeHtmlTemplates {
		@Template("<div class='{0}'>{1}{2}{3}</div>")
		SafeHtml event(SafeStyles style, SafeHtml thread, SafeHtml event, SafeHtml content);

		@Template("<span class='{0}' style='width: {1}ex'>{2}</span>")
		SafeHtml thread(SafeStyles style, int size, SafeHtml thread);

		@Template("<span title='{0}' style='margin-left: {1}ex'></span>")
		SafeHtml threadDetails(SafeHtml title, int margin);

		@Template("<span class='{0}'>{1}</span>")
		SafeHtml eventDetails(SafeStyles style, SafeHtml details);

		@Template("<span class='{0}'>{1}</span>")
		SafeHtml type(SafeStyles style, SafeHtml details);

		@Template("<em>null</em>")
		SafeHtml nullType();

		@Template("<span title='{0}'>{1}</span>")
		SafeHtml typeDetails(SafeHtml title, SafeHtml name);

		@Template("{0}{1}")
		SafeHtml method(SafeHtml call, SafeHtml args);

		@Template("<span class='{0}'>new</span><span class='{1}' title='{2}'>{3}></span>")
		SafeHtml initMethod(SafeStyles method, SafeStyles type, SafeHtml title, SafeHtml name);

		@Template("<span class='{0}' title='{1}'>{2}</span><span class='{3}'>.{4}</span>")
		SafeHtml methodCall(SafeStyles type, SafeHtml title, SafeHtml cls, SafeStyles method, SafeHtml name);

		@Template("<span class='{0}'>({1})</span>")
		SafeHtml args(SafeStyles style, SafeHtml args);

		@Template("")
		SafeHtml empty();
	}

	private static Templates templates = GWT.create(Templates.class);

	interface EventNames extends SafeHtmlTemplates {
		@Template("new")
		SafeHtml newObject();

		@Template("new[]")
		SafeHtml newArray();

		@Template("&rarr")
		SafeHtml methodEnter();

		@Template("&larr")
		SafeHtml methodReturn();

		@Template("&#x219A;")
		SafeHtml methodException();

		@Template("<em>read</em>")
		SafeHtml fieldRead();

		@Template("<em>write</em>")
		SafeHtml fieldWrite();

		@Template("<em>weave</em>")
		SafeHtml classWeave();

		@Template("<em>init</em>")
		SafeHtml classInit();

		@Template("<em>tag</em>")
		SafeHtml objectTagged();

		@Template("<em>free</em>")
		SafeHtml objectFreed();

		@Template("<em>unknown</em>")
		SafeHtml unknown();
	}

	private static EventNames events = GWT.create(EventNames.class);

	private final EventStyle style;
	private final Map<InstanceIdProxy, Integer> threads;

	public EventCell(EventStyle style, Map<InstanceIdProxy, Integer> threads) {
		this.style = style;
		this.threads = threads;
	}

	@Override
	public void render(Context c, EventProxy e, SafeHtmlBuilder sb) {
		if (e == null) return;

		SafeHtml thread = renderThread(e);
		SafeHtml event = renderEvent(e);

		SafeHtml content;
		if ((e.getEvent() & EventProxy.METHODS) == e.getEvent()) {
			content = renderMethod(e);
		} else {
			content = renderType(e);
		}

		sb.append(templates.event(style.eventCell(), thread, event, content));
	}

	SafeHtml renderThread(EventProxy e) {
		SafeHtml thread;
		if (threads.containsKey(e.getThread())) {
			int position = threads.get(e.getThread());
			SafeHtml title = SafeHtmlUtils.fromString(String.valueOf(e.getThread()));
			thread = templates.threadDetails(title, position);
		} else {
			thread = templates.empty();
		}
		return templates.thread(style.thread(), threads.size(), thread);
	}

	SafeHtml renderEvent(EventProxy e) {
		SafeHtml details = getEventDescription(e);
		return templates.eventDetails(style.event(), details);
	}

	SafeHtml renderType(EventProxy e) {
		SafeHtml details;
		if (e.getClazz() == null) {
			details = templates.nullType();
		} else {
			// TODO get the class name from a cache
			String fqname = "org.foo.Bar";
			String simple = "Bar";
			SafeHtml title = SafeHtmlUtils.fromString(fqname);
			SafeHtml name = SafeHtmlUtils.fromString(simple);
			details = templates.typeDetails(title, name);
		}
		return templates.type(style.type(), details);
	}

	SafeHtml renderMethod(EventProxy e) {
		ClazzIdProxy t = e.getClazz();
		MethodIdProxy m = e.getMethod();

		// TODO get details from a cache
		String className = "org.foo.Bar";
		String simpleClassName = "Bar";
		String methodName = "baz";

		SafeHtml call;
		if (methodName.equals("<init>")) {
			SafeHtml title = SafeHtmlUtils.fromString(className);
			SafeHtml name = SafeHtmlUtils.fromString(simpleClassName);
			call = templates.initMethod(style.method(), style.type(), title, name);
		} else {
			SafeHtml title = SafeHtmlUtils.fromString(className);
			SafeHtml name = SafeHtmlUtils.fromString(simpleClassName);
			SafeHtml method = SafeHtmlUtils.fromString(methodName);
			call = templates.methodCall(style.type(), title, name, style.method(), method);
		}

		SafeHtmlBuilder args = new SafeHtmlBuilder();
		if (e.getArgs() != null) {
			for (InstanceIdProxy arg : e.getArgs()) {
				if (arg != null) {
					// TODO get instance information
					args.appendEscaped(String.valueOf(arg));
				} else {
					args.appendHtmlConstant("<em>null</em>");
				}
			}
		}
		return templates.method(call, args.toSafeHtml());
	}

	SafeHtml getEventDescription(EventProxy e) {
		switch (e.getEvent()) {
			case EventProxy.OBJECT_ALLOCATED:
				return events.newObject();
			case EventProxy.ARRAY_ALLOCATED:
				return events.newArray();
			case EventProxy.METHOD_ENTER:
				return events.methodEnter();
			case EventProxy.METHOD_RETURN:
				return events.methodReturn();
			case EventProxy.FIELD_READ:
				return events.fieldRead();
			case EventProxy.FIELD_WRITE:
				return events.fieldWrite();
			case EventProxy.CLASS_WEAVE:
				return events.classWeave();
			case EventProxy.CLASS_INITIALIZED:
				return events.classWeave();
			case EventProxy.OBJECT_TAGGED:
				return events.objectTagged();
			case EventProxy.OBJECT_FREED:
				return events.objectFreed();
			case EventProxy.METHOD_EXCEPTION:
				return events.methodException();
			default:
				return events.unknown();
		}
	}

	SafeStyles getStyleName(EventProxy e) {
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
				return style.unknown();
		}
	}
}
