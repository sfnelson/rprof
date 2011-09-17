package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.client.request.ClazzProxy;
import nz.ac.vuw.ecs.rprofs.client.request.DataProvider;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.ClazzIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.FieldIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.InstanceIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.MethodIdProxy;

public class EventCell extends AbstractCell<EventProxy> {

	interface Templates extends SafeHtmlTemplates {
		@Template("<div class='{0}'>{1}{2}{3}</div>")
		SafeHtml event(String style, SafeHtml thread, SafeHtml event, SafeHtml content);

		@Template("<span class='{0}' style='width: {1}ex'>{2}</span>")
		SafeHtml thread(String style, int size, SafeHtml thread);

		@Template("<span title='{0}' style='margin-left: {1}ex'></span>")
		SafeHtml threadDetails(String title, int margin);

		@Template("<span class='{0}'>{1}</span>")
		SafeHtml eventDetails(String style, SafeHtml details);

		@Template("<span class='{0}'>{1}</span>")
		SafeHtml type(String style, SafeHtml details);

		@Template("<em>null</em>")
		SafeHtml nullType();

		@Template("<span title='{0}'>{1}</span>")
		SafeHtml typeDetails(String title, SafeHtml name);

		@Template("{0}{1}")
		SafeHtml method(SafeHtml call, SafeHtml args);

		@Template("<span class='{0}'>new</span> <span class='{1}' title='{2}'>{3}</span>")
		SafeHtml initMethod(String method, String type, String title, SafeHtml name);

		@Template("<span class='{0}' title='{1}'>{2}</span><span class='{3}'>.{4}</span>")
		SafeHtml methodCall(String type, String title, SafeHtml cls, String method, SafeHtml name);

		@Template("<span class='{0}'>({1})</span>")
		SafeHtml args(String style, SafeHtml args);

		@Template("<span class='{0}'><span class='{1}' title='{2}'>{3}</span>.<span class='{4}'>{5}</span>")
		SafeHtml field(String style, String type, String title, SafeHtml cls, String field, SafeHtml name);

		@Template("")
		SafeHtml empty();
	}

	private static Templates templates = GWT.create(Templates.class);

	interface EventNames extends SafeHtmlTemplates {
		@Template("new")
		SafeHtml newObject();

		@Template("new[]")
		SafeHtml newArray();

		@Template("&rarr;")
		SafeHtml methodEnter();

		@Template("&larr;")
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
	private final DataProvider provider;

	@Inject
	public EventCell(EventStyle style, DataProvider provider) {
		this.style = style;
		this.provider = provider;
	}

	@Override
	public void render(Context c, EventProxy e, SafeHtmlBuilder sb) {
		if (e == null) return;

		SafeHtml thread = renderThread(e);
		SafeHtml event = renderEvent(e);

		SafeHtml content;
		if ((e.getEvent() & EventProxy.METHODS) == e.getEvent()) {
			content = renderMethod(e);
		} else if ((e.getEvent() & EventProxy.FIELDS) == e.getEvent()) {
			content = renderField(e);
		} else {
			content = renderType(e);
		}

		sb.append(templates.event(style.eventCell(), thread, event, content));
	}

	SafeHtml renderThread(EventProxy e) {
		SafeHtml thread;
		if (provider.hasThread(e.getThread())) {
			int position = provider.getThreadIndex(e.getThread());
			String title = String.valueOf(e.getThread());
			thread = templates.threadDetails(title, position);
		} else {
			thread = templates.empty();
		}
		return templates.thread(style.thread(), provider.getNumThreads(), thread);
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
			String fqname;
			if (provider.hasEntity(e.getClazz())) {
				ClazzProxy clazz = provider.getEntity(e.getClazz());
				fqname = clazz.getName();
			} else {
				fqname = e.getClazz().getDatasetIndex()
						+ ":" + e.getClazz().getClassIndex();
			}
			String simple = getSimpleName(fqname);
			SafeHtml name = SafeHtmlUtils.fromString(simple);
			details = templates.typeDetails(fqname, name);
		}
		return templates.type(style.type(), details);
	}

	SafeHtml renderMethod(EventProxy e) {
		ClazzIdProxy t = e.getClazz();
		MethodIdProxy m = e.getMethod();

		String fqname;
		if (provider.hasEntity(t)) {
			ClazzProxy clazz = provider.getEntity(t);
			fqname = clazz.getName();
		} else {
			fqname = t.getDatasetIndex() + ":" + t.getClassIndex();
		}
		String simple = getSimpleName(fqname);

		String methodName;
		if (provider.hasEntity(m)) {
			methodName = provider.getEntity(m).getName();
		} else {
			methodName = m.getDatasetIndex()
					+ ":" + m.getClassIndex()
					+ "." + m.getAttributeIndex();
		}

		SafeHtml call;
		if (methodName.equals("<init>")) {
			String title = fqname;
			SafeHtml name = SafeHtmlUtils.fromString(simple);
			call = templates.initMethod(style.method(), style.type(), title, name);
		} else {
			String title = fqname;
			SafeHtml name = SafeHtmlUtils.fromString(simple);
			SafeHtml method = SafeHtmlUtils.fromString(methodName);
			call = templates.methodCall(style.type(), title, name, style.method(), method);
		}

		SafeHtmlBuilder args = new SafeHtmlBuilder();
		if (e.getArgs() != null) {
			boolean first = true;
			for (InstanceIdProxy arg : e.getArgs()) {
				if (first) first = false;
				else args.appendEscaped(", ");
				if (arg != null && arg.getValue() != 0) {
					String instance; // todo get instance from cache too?
					instance = arg.getThreadIndex() + "." + arg.getInstanceIndex();
					args.appendEscaped(instance);
				} else {
					args.appendHtmlConstant("<strong>null</strong>");
				}
			}
		}
		return templates.method(call, templates.args(style.args(), args.toSafeHtml()));
	}

	SafeHtml renderField(EventProxy e) {
		ClazzIdProxy t = e.getClazz();
		FieldIdProxy f = e.getField();

		String fqname;
		if (provider.hasEntity(t)) {
			ClazzProxy clazz = provider.getEntity(t);
			fqname = clazz.getName();
		} else {
			fqname = t.getDatasetIndex() + ":" + t.getClassIndex();
		}
		String simple = getSimpleName(fqname);

		String fieldName;
		if (provider.hasEntity(f)) {
			fieldName = provider.getEntity(f).getName();
		} else {
			fieldName = f.getDatasetIndex()
					+ ":" + f.getClassIndex()
					+ "." + f.getAttributeIndex();
		}

		String event = "";
		switch (e.getEvent()) {
			case EventProxy.FIELD_READ:
				event = style.fieldRead();
				break;
			case EventProxy.FIELD_WRITE:
				event = style.fieldWrite();
				break;
		}

		return templates.field(event, style.type(), fqname, SafeHtmlUtils.fromString(simple),
				style.field(), SafeHtmlUtils.fromString(fieldName));
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
				return style.unknown();
		}
	}

	private String getSimpleName(String fqname) {
		String simple = fqname;
		if (fqname.lastIndexOf('/') >= 0) {
			simple = fqname.substring(fqname.lastIndexOf('/') + 1);
		}
		return simple;
	}
}
