package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class ClassList extends Composite implements ClassListener, View {

	private static ClassListUiBinder uiBinder = GWT.create(ClassListUiBinder.class);

	private final Inspector server;
	private Button button;

	@UiField Style style;
	@UiField Grid panel;

	interface Style extends CssResource {
		String hide();
		String header();
	}

	interface ClassListUiBinder extends UiBinder<Widget, ClassList> {
	}

	public ClassList(Inspector server) {
		initWidget(uiBinder.createAndBindUi(this));

		this.server = server;
		button = new Button("Classes");

		server.addClassListener(this);

		panel.setWidget(0, 0, new InlineLabel("Methods"));
		panel.setWidget(0, 1, new InlineLabel("Equals"));
		panel.setWidget(0, 2, new InlineLabel("Hash"));
		panel.setWidget(0, 3, new InlineLabel("Class"));

		panel.getRowFormatter().addStyleName(0, style.header());
	}

	public void classesChanged(List<ClassRecord<MethodRecord>> records) {
		panel.clear();

		panel.resizeRows(1 + records.size());

		int i = 1;
		for (ClassRecord<MethodRecord> cr: records) {
			panel.setText(i, 0, String.valueOf(cr.getMethods().size()));
			panel.setText(i, 3, cr.name);
			
			String equals = "";
			String hash = "";
			for (MethodRecord mr: cr.getMethods()) {
				if (mr.name.equals("equals")) {
					equals = "true";
				}
				else if (mr.name.equals("hashCode")) {
					hash = "true";
				}
			}
			panel.setText(i, 1, equals);
			panel.setText(i, 2, hash);
			
			i++;
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

	@UiFactory
	Grid createGrid() {
		return new Grid(1, 4);
	}
}
