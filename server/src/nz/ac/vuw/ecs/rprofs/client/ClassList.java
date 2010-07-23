package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class ClassList extends Composite implements ClassListener, View {

	private static ClassListUiBinder uiBinder = GWT.create(ClassListUiBinder.class);
	
	private final Rprof_server server;
	private Button button;
	
	@UiField Style style;
	@UiField Panel panel;
	
	interface Style extends CssResource {
		String hide();
	}

	interface ClassListUiBinder extends UiBinder<Widget, ClassList> {
	}

	public ClassList(Rprof_server server) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.server = server;
		button = new Button("Classes");
		
		server.addClassListener(this);
	}

	public void classesChanged(List<ClassRecord> records) {
		panel.clear();
		for (ClassRecord cr: records) {
			panel.add(new ClassEntry(cr));
		}
	}

	public Widget getContentItem() {
		return this;
	}

	public Button getMenuButton() {
		return button;
	}
	
	public void refresh() {
		server.getClasses();
	}
}
