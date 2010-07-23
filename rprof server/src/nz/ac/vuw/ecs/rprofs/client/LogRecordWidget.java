package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
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

	LogRecord record;
	
	@UiField Label thread;
	@UiField HTML event;
	@UiField Label cname;
	@UiField Label mname;
	@UiField Label args;
	
	private int indent;

	public LogRecordWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		
		indent = 0;
	}
	
	public void init(LogRecord record, int indent) {
		this.indent = indent;
		this.record = record;
		
		thread.setText(record.threadId + "\t");
		event.getElement().getStyle().setPaddingLeft(indent, Unit.EM);
		cname.setText(record.className);
		mname.setText(record.methodName);
		
		if (record.event.contains("entered")) {
			event.setHTML("&rarr;");
		} else if (record.event.contains("exited")) {
			event.setHTML("&larr;");
		} else {
			event.setText(record.event);
		}
		
		StringBuilder args = new StringBuilder();
		if (record.arguments.length > 0) {
			args.append(record.arguments[0]);
			for (int i = 0; i < record.arguments.length; i++) {
				args.append(", ");
				args.append(record.arguments[i]);
			}
		}
		this.args.setText(args.toString());
	}
	
	public int getIndent() {
		return indent;
	}

}
