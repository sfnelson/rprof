package nz.ac.vuw.ecs.rprofs.client;

import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.FieldData;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;
import nz.ac.vuw.ecs.rprofs.client.data.MethodData;

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

public class LogRecordWidget extends Composite {

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

	@UiField HTML thread;
	@UiField HTML event;
	@UiField Label cname;
	@UiField Label mname;
	@UiField HTML args;

	private LogData record;

	public LogRecordWidget(LogData log) {
		initWidget(uiBinder.createAndBindUi(this));

		this.record = log;
	}

	public void setIndent(int indent) {
		event.getElement().getStyle().setPaddingLeft(indent, Unit.EM);
	}

	public void init(final nz.ac.vuw.ecs.rprofs.client.LogPanel.Style parent,
			final ClassData cr,
			final MethodData mr, final Map<Long, String> objects,
			final int thread) {

		// Set parent class
		switch (record.event) {
		case LogData.METHOD_ENTER:
			addStyleName(parent.methodEnter());
			break;
		case LogData.METHOD_RETURN:
			addStyleName(parent.methodReturn());
			break;
		case LogData.METHOD_EXCEPTION:
			addStyleName(parent.methodReturn());
			break;
		case LogData.OBJECT_ALLOCATED:
			addStyleName(parent.objectAllocated());
			break;
		case LogData.CLASS_WEAVE:
			addStyleName(parent.classWeave());
			break;
		case LogData.FIELD_READ:
			addStyleName(parent.fieldRead());
			break;
		case LogData.FIELD_WRITE:
			addStyleName(parent.fieldWrite());
			break;
		case LogData.CLASS_INITIALIZED:
			addStyleName(parent.classInitialized());
			break;
		case LogData.OBJECT_TAGGED:
			addStyleName(parent.objectTagged());
			break;
		case LogData.OBJECT_FREED:
			addStyleName(parent.objectFreed());
			break;
		}

		// Set internal class
		switch (record.event) {
		case LogData.METHOD_ENTER:
		case LogData.METHOD_RETURN:
		case LogData.METHOD_EXCEPTION:
			addStyleName(style.method());
			break;
		case LogData.OBJECT_ALLOCATED:
		case LogData.OBJECT_TAGGED:
		case LogData.CLASS_WEAVE:
		case LogData.CLASS_INITIALIZED:
		case LogData.OBJECT_FREED:
			addStyleName(style.object());
			break;
		case LogData.FIELD_READ:
		case LogData.FIELD_WRITE:
			addStyleName(style.field());
			break;
		}

		// Set thread text
		if (thread == 0) {
			this.thread.getElement().getStyle().setDisplay(Display.NONE);
		}
		else {
			this.thread.setTitle(getObjectString(objects, record.thread));
			this.thread.getElement().getStyle().setMarginLeft(thread - 0.5, Unit.EM);
		}

		// Set Class text
		if (cr != null) {
			cname.setText(cr.name);
		}
		else {
			cname.setText(String.valueOf(record.cnum));
		}
		cname.setTitle(String.valueOf(record.cnum));

		// Set Method/Field text
		switch (record.event) {
		case LogData.METHOD_ENTER:
		case LogData.METHOD_RETURN:
		case LogData.METHOD_EXCEPTION:
		case LogData.OBJECT_ALLOCATED:
			mname.setText(String.valueOf(record.mnum));
			mname.setTitle(String.valueOf(record.mnum));
			if (mr != null) {
				mname.setText(mr.name);
			}
			break;
		case LogData.FIELD_READ:
		case LogData.FIELD_WRITE:
			mname.setText(String.valueOf(record.mnum));
			if (cr != null) {
				for (FieldData fr: cr.getFields()) {
					if (fr.id == record.mnum) {
						mname.setText(fr.name);
					}
				}
			}
			break;
		}

		// Set event text
		switch (record.event) {
		case LogData.METHOD_ENTER:
			event.setHTML("&rarr;");
			break;
		case LogData.METHOD_RETURN:
			event.setHTML("&larr;");
			break;
		case LogData.METHOD_EXCEPTION:
			event.setHTML("&#x219A;");
			break;
		case LogData.OBJECT_ALLOCATED:
			event.setHTML("<strong>new</strong>");
			break;
		case LogData.FIELD_READ:
			event.setHTML("<em>read</em>");
			break;
		case LogData.FIELD_WRITE:
			event.setHTML("<em>write</em>");
			break;
		case LogData.CLASS_WEAVE:
			event.setHTML("<em>weave</em>");
			break;
		case LogData.CLASS_INITIALIZED:
			event.setHTML("<em>load</em>");
			break;
		case LogData.OBJECT_TAGGED:
			event.setHTML("<em>tagged</em>");
			break;
		case LogData.OBJECT_FREED:
			event.setHTML("<em>free</em>");
			break;
		default:
			event.setHTML("<em>unknown ("+record.event+")</em>");
		}

		// Initialize arguments (if appropriate)
		switch (record.event) {
		case LogData.METHOD_ENTER:
		case LogData.METHOD_RETURN:
		case LogData.METHOD_EXCEPTION:
			StringBuilder args = new StringBuilder("(");
			if (record.event == LogData.METHOD_RETURN && record.args.length > 0) {
				args.append("):");
				args.append(getObjectString(objects, record.args[0]));
			}
			else if (record.args.length > 0) {
				args.append(getObjectString(objects, record.args[0]));
				for (int i = 1; i < record.args.length; i++) {
					args.append(", ");
					args.append(getObjectString(objects, record.args[i]));
				}
				args.append(")");
			}
			else {
				args.append(")");
			}
			this.args.setHTML(args.toString());
			break;
		case LogData.OBJECT_ALLOCATED:
		case LogData.OBJECT_TAGGED:
		case LogData.OBJECT_FREED:
			this.args.setHTML(getObjectString(objects, record.args[0]) + " (" + record.cnum + ")");
			break;
		case LogData.CLASS_INITIALIZED:
			this.args.setHTML(cr.name + " (" + record.cnum + ")");
			break;
		case LogData.CLASS_WEAVE:
			this.args.setHTML(cr.name + " (" + record.cnum + ")");
			break;
		case LogData.FIELD_READ:
			this.args.setHTML(" (" + getObjectString(objects, record.args[0]) + ")");
			break;
		case LogData.FIELD_WRITE:
			if (record.args.length == 2) {
				this.args.setHTML(" = " + getObjectString(objects, record.args[1])
					+ " (" + getObjectString(objects, record.args[0]) + ")"
					);
			}
			else {
				this.args.setHTML(" (" + getObjectString(objects, record.args[0]) + ")");
			}
			break;
		}
	}

	private String getObjectString(Map<Long, String> objects, long arg) {
		if (arg == 0) {
			return "<strong>null</strong>";
		}

		String id = String.valueOf(arg>> 32) + "." + String.valueOf(arg & 0xffffffffl);
		if (objects.containsKey(arg)) {
			return objects.get(arg) + ":" + id;
		}
		else {
			return "<em>unknown</em>:" + id;
		}
	}
}
