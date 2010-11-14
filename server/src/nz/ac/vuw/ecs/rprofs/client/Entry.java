/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.EntryVisitor;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.client.history.History;
import nz.ac.vuw.ecs.rprofs.client.history.HistoryManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class Entry extends Composite implements HasClickHandlers, HasEntries, EntryVisitor<Void> {

	interface EntryUiBinder extends UiBinder<Widget, Entry> {}

	private EntryUiBinder uiBinder = GWT.create(EntryUiBinder.class);

	interface Style extends CssResource {
		String string();
		String object();
		String count();
		String flag();
		String open();
		String number();
		String active();
	}

	@UiField Style style;

	@UiField EntryPanel entries;
	@UiField Panel children;

	private final Report report;
	private final Report.Entry target;
	private final Anchor[] fields;

	public Entry(Report report, Report.Entry entry) {
		this.report = report;
		this.target = entry;
		this.fields = new Anchor[report.headings.length];

		initWidget(uiBinder.createAndBindUi(this));
		initFields();

		entry.visit(this);
	}

	public Entry(Report report) {
		this.report = report;
		this.target = null;
		this.fields = new Anchor[report.headings.length];

		initWidget(uiBinder.createAndBindUi(this));
		initFields();

		for (int i = 0; i < report.headings.length; i++) {
			setHTML(report.types[i], report.headings[i], fields[i]);
			
			String heading = report.headingTitle[i];
			if (heading == null) {
				heading = report.headings[i];
			}
			setTitle(report.types[i], report.flags[i], heading, fields[i]);
		}
	}
	
	private void initFields() {
		for (int i = 0; i < fields.length; i++) {
			Anchor field = new Anchor();
			field.getElement().removeAttribute("href");
			switch (report.types[i]) {
			case OBJECT: field.setStyleName(style.object()); break;
			case COUNT: field.setStyleName(style.count()); break;
			case NAME: field.setStyleName(style.string()); break;
			case FLAG: field.setStyleName(style.flag()); break;
			}			
			entries.add(field);
			fields[i] = field;
		}
	}

	@Override
	public Void visitClassEntry(ClassEntry entry) {
		String name = entry.cls.getClassName();
		String title = entry.cls.getClassName() + " (" + entry.cls.id + ")";
		visitEntry("", "", name, title, entry.instances, entry.instances, entry);
		return null;
	}

	@Override
	public Void visitInstanceEntry(InstanceEntry entry) {
		visitEntry("", "", "", "", entry.id(), entry.id(), entry);
		return null;
	}

	@Override
	public Void visitPackageEntry(PackageEntry entry) {
		String pkg = entry.pkg;
		String title = entry.pkg;
		if (pkg.equals("")) {
			pkg = "<em>default</em>";
			title = "default";
		}
		visitEntry(pkg, title, entry.classes, entry.classes, entry.instances, entry.instances, entry);
		return null;
	}

	private void visitEntry(String pkg, String pkgH, Object cls, Object clsH,
			Object id, Object idH, Report.Entry entry) {
		int index = 0;

		if (report.headings[index].equals("Package")) {
			setHTML(report.types[index], pkg, fields[index]);
			setTitle(report.types[index], report.flags[index], pkgH, fields[index]);
			index++;
		}
		if (report.headings[index].equals("Class")) {
			setHTML(report.types[index], cls, fields[index]);
			setTitle(report.types[index], report.flags[index], clsH, fields[index]);
			index++;
		}
		if (report.headings[index].equals("Instance")
				|| report.headings[index].equals("Method")
				|| report.headings[index].equals("Field")) {
			setHTML(report.types[index], id, fields[index]);
			setTitle(report.types[index], report.flags[index], idH, fields[index]);
			index++;
		}

		for (Object value: entry.values) {
			setHTML(report.types[index], value, fields[index]);
			setTitle(report.types[index], report.flags[index], value, fields[index]);
			index++;
		}
	}

	public Report.Entry getTarget() {
		return target;
	}

	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return entries.addClickHandler(handler);
	}

	@Override
	public void add(Entry child) {
		children.add(child);
	}

	private boolean open = false;

	public boolean isOpen() {
		return open;
	}

	public void open() {
		open = true;
		addStyleName(style.open());
	}

	public void close() {
		open = false;
		removeStyleName(style.open());
	}

	public void setPopulated(boolean value) {
		populated = value;
	}

	public boolean isPopulated() {
		return populated;
	}

	private boolean populated = false;

	protected void setHTML(Report.Type type, final Object value, final Anchor field) {
		String html;
		switch (type) {
		case OBJECT:
			if (value instanceof Long) {
				final long arg = (Long) value;
				if (arg == 0) {
					html = "<strong>null</strong>";
				}
				else {
					html = String.valueOf(arg>> 32) + "." + String.valueOf(arg & 0xffffffffl);
					field.addStyleName(style.active());
					field.addMouseOverHandler(new MouseOverHandler() {
						@Override
						public void onMouseOver(MouseOverEvent event) {
							History h = HistoryManager.getInstance().getHistory();
							h.id = (Long) value;
							field.setHref("#"+h.toString());
						}
					});
				}
				break;
			}
			else {
				html = String.valueOf(value);
				break;
			}
		case COUNT:
			if (value instanceof Integer) {
				if (((Integer) value) == 0) {
					html = "";
					break;
				}
			}
			html = String.valueOf(value);
			break;
		case NAME:
			if (value instanceof Integer) {
				field.addStyleName(style.number());
			}
			html = String.valueOf(value);
			break;
		case FLAG:
			if (value instanceof Integer) {
				if (((Integer) value) == 0) {
					html = "";
				}
				else {
					html = "&#9888;";
				}
			}
			else {
				html = String.valueOf(value);
			}
			break;
		default:
			html = String.valueOf(value);
			break;
		}
		field.setHTML(html);
	}

	protected void setTitle(Report.Type type, String[] flags, Object value, Anchor field) {
		String title = "";
		switch (type) {
		case FLAG:
			if (value instanceof Integer) {
				int val = (Integer) value;
				for (int flag = 0; val != 0; flag++, val = val / 2) {
					if (val % 2 == 1) {
						String f = flags[flag];
						if (title.length() == 0) title = f;
						else title += '\n' + f;
					}
				}
			}
			else {
				title = String.valueOf(value);
			}
			break;
		case OBJECT:
			if (value instanceof Long) {
				long arg = (Long) value;
				if (arg == 0) {
					title = "null";
					break;
				}
				title = String.valueOf(arg>> 32) + "." + String.valueOf(arg & 0xffffffffl);
				break;
			}
		default:
			title = String.valueOf(value);
		}
		field.setTitle(title);
	}

	@UiFactory
	EntryPanel createEntryPanel() {
		return new EntryPanel(this);
	}

	static class EntryPanel extends FlowPanel implements HasClickHandlers {
		private Entry parent;
		public EntryPanel(Entry entry) {
			this.parent = entry;
		}
		public Entry parent() {
			return parent;
		}
		@Override
		public HandlerRegistration addClickHandler(ClickHandler handler) {
			return addDomHandler(handler, ClickEvent.getType());
		}

	}
}
