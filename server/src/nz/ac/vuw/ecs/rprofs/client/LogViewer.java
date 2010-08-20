package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class LogViewer extends Composite implements LogListener, ClassListener, View {

	public static final int RECORD_BATCH_LIMIT = 1000;

	private static LogViewerUiBinder uiBinder = GWT
	.create(LogViewerUiBinder.class);

	interface LogViewerUiBinder extends UiBinder<Widget, LogViewer> {
	}

	interface Style extends CssResource {
		String filter();
		String active();
		String methodEnter();
		String methodReturn();
		String objectAllocated();
		String classWeave();
		String fieldRead();
		String fieldWrite();
		String objectTagged();
		String classInitialized();
	}

	@UiField Style style;

	private final Inspector server;

	private final List<LogRecordWidget> records = new ArrayList<LogRecordWidget>();
	private int available = 0;

	private final Map<Long, String> objects = Collections.newMap();
	private final Map<Integer, ClassRecord<MethodRecord, FieldRecord>> classes = Collections.newMap();
	private final Map<Long, Integer> indent = Collections.newMap();
	private final Map<Long, Integer> threads = Collections.newMap();

	@UiField Panel filters;
	@UiField Panel content;

	private Button button;

	private boolean pending = false;

	public LogViewer(Inspector rprofServer) {
		initWidget(uiBinder.createAndBindUi(this));

		server = rprofServer;
		server.addEventListener(this);
		server.addClassListener(this);
		button = new Button("Events");
		
		filters.add(new Filter("Allocate", style.objectAllocated()));
		filters.add(new Filter("Weave", style.classWeave()));
		filters.add(new Filter("Invoke", style.methodEnter()));
		filters.add(new Filter("Return", style.methodReturn()));
		filters.add(new Filter("Read", style.fieldRead()));
		filters.add(new Filter("Write", style.fieldWrite()));
		filters.add(new Filter("Loaded", style.classInitialized(), false));
		filters.add(new Filter("Tagged", style.objectTagged(), false));
	}

	public void logsChanged(int number) {
		content.clear();
		records.clear();
		objects.clear();
		indent.clear();
		threads.clear();
		available = number;

		System.out.println(number + " logs available");

		server.getLogs(0, RECORD_BATCH_LIMIT);
	}

	private int getIndent(long threadId) {
		if (!indent.containsKey(threadId)) {
			addThread(threadId);
		}
		return indent.get(threadId);
	}

	private void changeIndent(long threadId, int change) {
		if (!indent.containsKey(threadId)) {
			addThread(threadId);
		}

		int indent = this.indent.get(threadId) + change;
		if (indent < 0) indent = 0;
		this.indent.put(threadId, indent);
	}

	private void addThread(long threadId) {
		threads.put(threadId, threads.size());
		if (indent.containsKey(0l)) {
			indent.put(threadId, indent.get(0l));
		}
		else {
			indent.put(threadId, 0);
		}

		content.getElement().getStyle().setPaddingLeft(threads.size(), Unit.EM);
	}

	public void logsAvailable(List<LogRecord> lr, int offset) {
		for (LogRecord r: lr) {
			LogRecordWidget w = new LogRecordWidget(r);

			// Set indent for the record, based on current thread's indent level
			switch (r.event) {
			case LogRecord.METHOD_RETURN:
				changeIndent(r.thread, -1);
				w.setIndent(getIndent(r.thread));
				break;
			case LogRecord.METHOD_ENTER:
				w.setIndent(getIndent(r.thread));
				changeIndent(r.thread, 1);
				break;
			default:
				w.setIndent(getIndent(r.thread));
				break;
			}

			w.init(style, classes.get(r.cnum), objects, threads.get(r.thread));
			content.add(w);
		}

		if (lr.size() + offset < available) {
			final int newOffset = lr.size() + offset;
			final int limit = Math.min(available - newOffset, RECORD_BATCH_LIMIT);
			new Timer() {
				public void run() {
					server.getLogs(newOffset, limit);
				}
			}.schedule(1);
		}
	}

	public Widget getContentItem() {
		return this;
	}

	public Button getMenuButton() {
		return button;
	}

	public void refresh() {
		if (classes.isEmpty()) {
			pending = true;
			server.getClasses();
		}
		else {
			server.refreshLogs();
		}
	}

	@Override
	public void classesChanged(List<ClassRecord<MethodRecord, FieldRecord>> cr) {
		classes.clear();

		for (ClassRecord<MethodRecord, FieldRecord> c: cr) {
			classes.put(c.id, c);
		}

		if (pending) {
			pending = false;
			server.refreshLogs();
		}
	}

	private class Filter extends Composite implements ClickHandler {
		private final String filter;
		private boolean active = false;
		
		public Filter(String name, String filter) {
			this(name, filter, true);
		}
		
		public Filter(String name, String filter, boolean active) {
			this.filter = filter;
			this.active = active;
			
			Anchor a = new Anchor(name);
			a.addClickHandler(this);
			initWidget(a);
			
			setStyleName(style.filter());
			if (active) {
				setActive();
			} else {
				setInactive();
			}
		}
		
		public void setActive() {
			active = true;
			addStyleName(style.active());
			LogViewer.this.removeStyleName(filter);
		}
		
		public void setInactive() {
			removeStyleName(style.active());
			LogViewer.this.addStyleName(filter);
			active = false;
		}

		@Override
		public void onClick(ClickEvent event) {
			if (active) {
				setInactive();
			} else {
				setActive();
			}
		}
	}
}
