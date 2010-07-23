package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class LogViewer extends Composite implements LogListener, View {
	
	public static final int RECORD_BATCH_LIMIT = 100;

	private static LogViewerUiBinder uiBinder = GWT
			.create(LogViewerUiBinder.class);

	interface LogViewerUiBinder extends UiBinder<Widget, LogViewer> {
	}
	
	private final Rprof_server server;
	
	private final List<LogRecordWidget> records = new ArrayList<LogRecordWidget>();
	private int available = 0;

	@UiField Panel panel;
	
	private Button button;

	public LogViewer(Rprof_server rprofServer) {
		initWidget(uiBinder.createAndBindUi(this));
		
		server = rprofServer;
		server.addEventListener(this);
		button = new Button("Events");
	}

	public void logsChanged(int number) {
		panel.clear();
		records.clear();
		available = number;
		
		System.out.println(number + " logs available");
		
		/*
		for (int i = 0; i< number; i++) {
			LogRecordWidget widget = new LogRecordWidget();
			records.add(widget);
			panel.add(widget);
		}
		*/
		
		server.getLogs(0, RECORD_BATCH_LIMIT);
	}
	
	public void logsAvailable(List<LogRecord> lr, int offset) {
		
		int indent = 0;
		
		System.out.println("logs " + offset + " to " + (offset + lr.size()) + " available");
		
		int i = 0;
		
		if (offset > 0) {
			LogRecordWidget w = records.get(records.size() - 1);
			indent = w.getIndent();
			if (w.record.event.contains("entered")) {
				indent++;
				
				if (0 < lr.size()) {
					LogRecord n = lr.get(0);
					LogRecord r = w.record;
					if (n.classNumber == r.classNumber && r.methodNumber == n.methodNumber) {
						i++;
						w.event.setText("");
						indent--;
					}
				}
			}
		}
		
		for (; i < lr.size(); i++) {
			LogRecord r = lr.get(i);
			if (r.event.contains("exited")) {
				indent--;
			}
			LogRecordWidget w = new LogRecordWidget();
			records.add(w);
			w.init(r, indent);
			panel.add(w);
			if (r.event.contains("entered")) {
				indent++;
				
				if (i + 1 < lr.size()) {
					LogRecord n = lr.get(i + 1);
					if (n.classNumber == r.classNumber && r.methodNumber == n.methodNumber) {
						i++;
						w.event.setText("");
						indent--;
					}
				}
			}
		}
		
		if (lr.size() + offset < available) {
			int newOffset = lr.size() + offset;
			int limit = Math.min(available - newOffset, RECORD_BATCH_LIMIT);
			server.getLogs(newOffset, limit);
		}
	}

	public Widget getContentItem() {
		return this;
	}

	public Button getMenuButton() {
		return button;
	}

	public void refresh() {
		server.refreshLogs();
	}

}
