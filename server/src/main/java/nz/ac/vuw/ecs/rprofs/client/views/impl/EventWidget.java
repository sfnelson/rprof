package nz.ac.vuw.ecs.rprofs.client.views.impl;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.request.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.request.MethodProxy;
import nz.ac.vuw.ecs.rprofs.client.ui.InstanceLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class EventWidget extends Composite {

	interface Style extends CssResource {
		public String objectAllocated();
		public String arrayAllocated();
		public String methodEnter();
		public String methodExit();
		public String methodException();
		public String fieldRead();
		public String fieldWrite();
		public String classWeave();
		public String classInit();
		public String objectTagged();
		public String objectFreed();
	}

	private static EventWidgetUiBinder uiBinder = GWT
	.create(EventWidgetUiBinder.class);

	interface EventWidgetUiBinder extends UiBinder<Widget, EventWidget> {
	}

	@UiField Style style;

	@UiField InstanceLabel thread;
	@UiField HTML event;
	@UiField Label type;
	@UiField Label method;
	@UiField Label field;
	@UiField Panel args;

	public EventWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setEvent(EventProxy event) {
		setThread(event.getThread());
		setEvent(event.getEvent());
		setType(event.getType());
		setMethod(event.getMethod());
		setField(event.getField());
		setArgs(event.getArguments());
	}

	public void setArgs(List<InstanceProxy> arguments) {
		args.clear();
		if (arguments == null) {
			return;
		}
		for (InstanceProxy arg: arguments) {
			InstanceLabel label = new InstanceLabel();
			label.setInstance(arg);
			args.add(label);
		}
	}

	public void setThread(InstanceProxy thread) {
		this.thread.setInstance(thread);
	}

	public void setEvent(int event) {
		String text = null;
		String type = null;

		switch (event) {
		case EventProxy.OBJECT_ALLOCATED:
			text = "new";
			type = style.objectAllocated();
			break;
		case EventProxy.ARRAY_ALLOCATED:
			text = "new[]";
			type = style.arrayAllocated();
			break;
		case EventProxy.METHOD_ENTER:
			text = "&rarr;";
			type = style.methodEnter();
			break;
		case EventProxy.METHOD_RETURN:
			text = "&larr;";
			type = style.methodExit();
			break;
		case EventProxy.FIELD_READ:
			text = "<em>read</em>";
			type = style.fieldRead();
			break;
		case EventProxy.FIELD_WRITE:
			text = "<em>write</em>";
			type = style.fieldWrite();
			break;
		case EventProxy.CLASS_WEAVE:
			text = "<em>weave</em>";
			type = style.classWeave();
			break;
		case EventProxy.CLASS_INITIALIZED:
			text = "<em>init</em>";
			type = style.classInit();
			break;
		case EventProxy.OBJECT_TAGGED:
			text = "<em>tag</em>";
			type = style.objectTagged();
			break;
		case EventProxy.OBJECT_FREED:
			text = "<em>free</em>";
			type = style.objectFreed();
			break;
		case EventProxy.METHOD_EXCEPTION:
			text = "&#x219A;";
			type = style.methodException();
			break;
		}

		this.event.setHTML(text);
		this.setStyleName(type);
	}

	public void setType(ClassProxy cls) {
		if (cls == null) {
			this.type.setText("null");
		}
		else {
			this.type.setText(cls.getClassName());
			this.type.setTitle(cls.getName());
		}
	}

	public void setMethod(MethodProxy method) {
		if (method != null) {
			this.method.setText(method.getName());
			this.method.setTitle(method.getDescription());
		}
	}

	public void setField(FieldProxy field) {
		if (field != null) {
			this.field.setText(field.getName());
			this.field.setTitle(field.getDescription());
		}
	}
}
