package nz.ac.vuw.ecs.rprofs.client;

import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;
import nz.ac.vuw.ecs.rprofs.client.data.MethodData;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.ArrayAllocated;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.ClassInitialized;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.ClassWeave;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.FieldRead;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.FieldWrite;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.MethodEnter;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.MethodException;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.MethodReturn;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.ObjectAllocated;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.ObjectFreed;
import nz.ac.vuw.ecs.rprofs.client.data.LogInfo.ObjectTagged;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class LogRecordWidget extends Composite implements LogData.Visitor {

	private static LogRecordWidgetUiBinder uiBinder = GWT
	.create(LogRecordWidgetUiBinder.class);

	interface LogRecordWidgetUiBinder extends UiBinder<Widget, LogRecordWidget> {
	}

	interface Style extends CssResource {
		public String object();
		public String method();
		public String field();
	}

	@UiField Style style;
	LogPanel.Style parent;

	@UiField HTML thread;
	@UiField HTML event;
	@UiField Label cname;
	@UiField Label mname;
	@UiField HTML args;

	private LogData record;

	public LogRecordWidget(LogData log, LogPanel.Style parent) {
		initWidget(uiBinder.createAndBindUi(this));

		this.record = log;
		this.parent = parent;

		log.visit(this);
	}

	public void setIndent(int indent) {
		event.getElement().getStyle().setPaddingLeft(indent, Unit.EM);
	}

	public void init(final LogPanel.Style parent,
			final ClassData cr,
			final MethodData mr, final Map<Long, String> objects,
			final int thread) {

		// Set thread text
		if (thread == 0) {
			this.thread.getElement().getStyle().setDisplay(Display.NONE);
		}
		else {
			this.thread.setTitle(String.valueOf(record.thread));
			this.thread.getElement().getStyle().setMarginLeft(thread - 0.5, Unit.EM);
		}
	}

	@Override
	public void visitArrayAllocatedEvent(ArrayAllocated event) {
		addStyleName(parent.arrayAllocated());
		addStyleName(style.object());

		cname.setText(event.getType().toString());

		this.event.setHTML("<strong>new</strong>");

		args.setText("[" + event.getParameters() + "]");
	}

	@Override
	public void visitClassInitializatedEvent(ClassInitialized event) {
		addStyleName(parent.classInitialized());
		addStyleName(style.object());

		cname.setText(event.getType().toString());

		this.event.setHTML("<em>load</em>");
	}

	@Override
	public void visitClassWeaveEvent(ClassWeave event) {
		addStyleName(parent.classWeave());
		addStyleName(style.object());

		cname.setText(event.getType().toString());

		this.event.setHTML("<em>weave</em>");
	}

	@Override
	public void visitFieldReadEvent(FieldRead event) {
		addStyleName(parent.fieldRead());
		addStyleName(style.field());

		cname.setText(event.getType().toString());
		mname.setText(event.getField().toFieldString());

		this.event.setHTML("<em>read</em>");

		this.args.setHTML(" (" + record.args[0] + ")");
	}

	@Override
	public void visitFieldWriteEvent(FieldWrite event) {
		addStyleName(parent.fieldWrite());
		addStyleName(style.field());

		cname.setText(event.getType().toString());
		mname.setText(event.getField().toFieldString());


		this.event.setHTML("<em>write</em>");

		if (record.args.length == 2) {
			this.args.setHTML(" = " + record.args[1]
			                                      + " (" + record.args[0] + ")"
			);
		}
		else {
			this.args.setHTML(" (" + record.args[0] + ")");
		}
	}

	@Override
	public void visitMethodEnterEvent(MethodEnter event) {
		addStyleName(parent.methodEnter());
		addStyleName(style.method());

		cname.setText(event.getType().toString());
		mname.setText(event.getMethod().getName());

		this.event.setHTML("&rarr;");
		args.setHTML(event.getParameters().toString());
	}

	@Override
	public void visitMethodExceptionEvent(MethodException event) {
		addStyleName(parent.methodReturn());
		addStyleName(style.method());

		cname.setText(event.getType().toString());
		mname.setText(event.getMethod().getName());

		this.event.setHTML("&#x219A;");
		args.setHTML(event.getThrowable().toString());
	}

	@Override
	public void visitMethodReturnEvent(MethodReturn event) {
		addStyleName(parent.methodReturn());
		addStyleName(style.method());

		cname.setText(event.getType().toString());
		mname.setText(event.getMethod().getName());

		this.event.setHTML("&larr;");
		args.setHTML(event.getReturnValue().toString());
	}

	@Override
	public void visitObjectAllocatedEvent(ObjectAllocated event) {
		addStyleName(parent.objectAllocated());
		addStyleName(style.object());

		cname.setText(event.getType().toString());
		mname.setText(event.getConstructor().toString());

		this.event.setHTML("<strong>new</strong>");

		args.setHTML(event.getTarget().toString());
	}

	@Override
	public void visitObjectFreedEvent(ObjectFreed event) {
		addStyleName(parent.objectFreed());
		addStyleName(style.object());

		cname.setText(event.getType().toString());

		this.event.setHTML("<em>free</em>");
	}

	@Override
	public void visitObjectTaggedEvent(ObjectTagged event) {
		addStyleName(parent.objectTagged());
		addStyleName(style.object());

		cname.setText(event.getType().toString());

		this.event.setHTML("<em>tagged</em>");
	}
}
