package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class ClassList extends Composite {

	private static ClassListUiBinder uiBinder = GWT.create(ClassListUiBinder.class);

	@UiField Style style;
	@UiField(provided=true) ClassEntry heading;
	@UiField Panel container;

	private List<String> classes = new ArrayList<String>();
	private Map<String, ClassRecord<MethodRecord, FieldRecord>> recordMap = Collections.newMap();
	private Map<String, ClassEntry> entryMap = new HashMap<String, ClassEntry>();

	private boolean expanded = false;
	
	interface Style extends CssResource {
	}

	interface ClassListUiBinder extends UiBinder<Widget, ClassList> {
	}

	public ClassList(String pkg, Inspector server) {
		heading = new ClassEntry(pkg);

		initWidget(uiBinder.createAndBindUi(this));
	}

	public void add(ClassRecord<MethodRecord, FieldRecord> cr) {
		String name = cr.getClassName();
		classes.add(name);
		recordMap.put(name, cr);

		int equals = 0;
		int hashes = 0;

		for (MethodRecord mr: cr.getMethods()) {
			if ("equals".equals(mr.name) && "(Ljava/lang/Object;)Z".equals(mr.desc)) {
				equals = 1;
			}
			else if ("hashCode".equals(mr.name) && "()I".equals(mr.desc)) {
				hashes = 1;
			}
		}

		ClassEntry ce = new ClassEntry(name, cr.getMethods().size(), cr.instances, equals, hashes);
		entryMap.put(name, ce);

		heading.add(ce);
	}

	@UiHandler("heading")
	public void expand(ClickEvent ev) {
		if (expanded) {
			container.clear();
		}
		else {
			Collections.sort(classes);
			for (String cls: classes) {
				ClassEntry entry = entryMap.get(cls);
				container.add(entry);
			}
		}
		
		expanded = !expanded;
	}
}
