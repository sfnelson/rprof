package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class LogView extends Composite implements LogListener, ClassListener, View {

	public static final int RECORD_BATCH_LIMIT = 1000;

	private static LogViewUiBinder uiBinder = GWT.create(LogViewUiBinder.class);

	interface LogViewUiBinder extends UiBinder<Widget, LogView> {
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
		String objectFreed();
	}

	@UiField Style style;

	private final Inspector server;

	private final List<LogRecordWidget> records = new ArrayList<LogRecordWidget>();
	private int available = 0;
	private boolean inMain = false;
	private int mainClass = 0;
	private int mainMethod = 0;
	
	private boolean showAll = false;

	private final Map<Long, String> objects = Collections.newMap();
	private final Map<Integer, ClassRecord<MethodRecord, FieldRecord>> classes = Collections.newMap();
	private final Map<Long, Integer> indent = Collections.newMap();
	private final Map<Long, Integer> threads = Collections.newMap();

	@UiField Panel filters;
	@UiField Panel content;

	private Button button;

	private boolean pending = false;

	public LogView(Inspector rprofServer) {
		initWidget(uiBinder.createAndBindUi(this));

		server = rprofServer;
		server.addClassListener(this);
		button = new Button("Events");

		filters.add(new Filter("Allocate", style.objectAllocated()));
		filters.add(new Filter("Weave", style.classWeave()));
		filters.add(new Filter("Invoke", style.methodEnter()));
		filters.add(new Filter("Return", style.methodReturn()));
		filters.add(new Filter("Read", style.fieldRead()));
		filters.add(new Filter("Write", style.fieldWrite()));
		filters.add(new Filter("Load", style.classInitialized(), false));
		filters.add(new Filter("Tag", style.objectTagged(), false));
		filters.add(new Filter("Free", style.objectFreed()));
		
		CheckBox all = new CheckBox("All Events");
		all.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				showAll = event.getValue();
				refresh();
			}
		});
		all.getElement().getStyle().setFloat(Float.RIGHT);
		filters.add(all);
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


	@Override
	public void logsAvailable(int type, int available, LogCallback callback) {
		content.clear();
		records.clear();
		objects.clear();
		indent.clear();
		threads.clear();
		this.available = available;
		inMain = false;
		mainClass = 0;
		mainMethod = 0;
		
		callback.doRequest(0, RECORD_BATCH_LIMIT);
	}

	@Override
	public void logsAvailable(int type, int offset, int limit,
			Collection<LogRecord> result, final LogCallback callback) {

		for (LogRecord r: result) {
			ClassRecord<MethodRecord, FieldRecord> cr = classes.get(r.cnum);
			MethodRecord mr = null;
			if (cr != null) {
				switch (r.event) {
				case LogRecord.OBJECT_FREED:
					objects.remove(r.args[0]);
					break;
				case LogRecord.OBJECT_ALLOCATED:
				case LogRecord.OBJECT_TAGGED:
					objects.put(r.args[0], cr.name);
				case LogRecord.METHOD_ENTER:
				case LogRecord.METHOD_RETURN:
					for (MethodRecord m: cr.getMethods()) {
						if (m.id == r.mnum) {
							mr = m;
						}
					}
				}
			}

			if (!inMain) {
				if (r.event == LogRecord.METHOD_ENTER) {
					inMain = (mr != null) && mr.isMain();
					if (inMain) {
						mainClass = r.cnum;
						mainMethod = r.mnum;
					}
				}
			}
			
			int indent = 0;
			
			// Determine the indent for the record, based on current thread's indent level
			switch (r.event) {
			case LogRecord.METHOD_RETURN:
				changeIndent(r.thread, -1);
				indent = getIndent(r.thread);
				break;
			case LogRecord.METHOD_ENTER:
				indent = getIndent(r.thread);
				changeIndent(r.thread, 1);
				break;
			default:
				indent = getIndent(r.thread);
				break;
			}
			
			if (inMain || showAll) {
				LogRecordWidget w = new LogRecordWidget(r);
				w.setIndent(indent);
				w.init(style, cr, mr, objects, threads.get(r.thread));
				content.add(w);
				
				if (mr != null && r.event == LogRecord.METHOD_RETURN && r.cnum == mainClass && r.mnum == mainMethod) {
					inMain = false;
				}
			}
		}

		if (result.size() + offset < available) {
			final int newOffset = result.size() + offset;
			final int newLimit = Math.min(available - newOffset, RECORD_BATCH_LIMIT);
			new Timer() {
				public void run() {
					callback.doRequest(newOffset, newLimit);
				}
			}.schedule(10);
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
			server.getLogs(LogRecord.ALL, this);
		}
	}

	@Override
	public void classesChanged(Collection<ClassRecord<MethodRecord, FieldRecord>> cr) {
		classes.clear();

		for (ClassRecord<MethodRecord, FieldRecord> c: cr) {
			classes.put(c.id, c);
		}

		if (pending) {
			pending = false;
			server.getLogs(LogRecord.ALL, this);
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
			LogView.this.removeStyleName(filter);
		}

		public void setInactive() {
			removeStyleName(style.active());
			LogView.this.addStyleName(filter);
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
