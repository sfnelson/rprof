/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class PackageList extends Composite implements ClassListener, View {

	private static PackageListUiBinder uiBinder = GWT
			.create(PackageListUiBinder.class);

	interface PackageListUiBinder extends UiBinder<Widget, PackageList> {
	}
	
	interface Style extends CssResource {
		String even();
	}

	private final Inspector server;
	private final Button button;
	
	@UiField Style style;
	@UiField Panel container;

	public PackageList(Inspector server) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.server = server;
		this.button = new Button("Classes");
		
		server.addClassListener(this);
	}

	@Override
	public void classesChanged(List<ClassRecord<MethodRecord, FieldRecord>> classes) {
		container.clear();
		
		List<String> packages = new ArrayList<String>();
		Map<String, ClassList> packageMap = new HashMap<String, ClassList>();
		
		for (ClassRecord<MethodRecord, FieldRecord> cr: classes) {
			String pkg = cr.getPackage();
			
			ClassList list = packageMap.get(pkg);
			
			if (list == null) {
				list = new ClassList(pkg, server);
				packageMap.put(pkg, list);
				packages.add(pkg);
			}
			
			list.add(cr);
		}
		
		Collections.sort(packages);
		
		int i = 0;
		for (String pkg: packages) {
			i++;
			ClassList cl = packageMap.get(pkg);
			if (i%2 == 0) {
				cl.heading.addStyleName(style.even());
			}
			container.add(cl);
		}
	}

	@Override
	public Widget getContentItem() {
		return this;
	}

	@Override
	public Button getMenuButton() {
		return button;
	}
	
	@Override
	public void refresh() {
		server.getClasses();
	}
}
