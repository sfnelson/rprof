/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ClassEntry extends Composite implements HasClickHandlers {

	private static ClassEntryUiBinder uiBinder = GWT
			.create(ClassEntryUiBinder.class);

	interface Style extends CssResource {
		String defaultPackage();
	}
	
	interface ClassEntryUiBinder extends UiBinder<Widget, ClassEntry> {
	}

	@UiField Style style;
	@UiField Label packageLabel;
	@UiField Label classLabel;
	@UiField Label methodsLabel;
	@UiField Label instancesLabel;
	@UiField Label equalsLabel;
	@UiField Label hashLabel;
	
	private int classes;
	private int methods;
	private int instances;
	private int equals;
	private int hashes;
	
	public ClassEntry(String cName, int methods, int instances, int equals, int hashes) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.classes = 1;
		this.methods = methods;
		this.instances = instances;
		this.equals = equals;
		this.hashes = hashes;

		update();
		setField(classLabel, cName);
	}
	
	public ClassEntry() {
		initWidget(uiBinder.createAndBindUi(this));

		packageLabel.setText("Packages");
		classLabel.setText("Classes");
		methodsLabel.setText("Methods");
		instancesLabel.setText("Instances");
		equalsLabel.setText("Equals");
		hashLabel.setText("Hash");
	}
	
	public ClassEntry(String pName) {
		initWidget(uiBinder.createAndBindUi(this));

		setField(packageLabel, pName);
		
		if ("".equals(pName)) {
			packageLabel.setText("default");
			packageLabel.addStyleName(style.defaultPackage());
		}
		
		classes = 0;
		methods = 0;
		instances = 0;
		equals = 0;
		hashes = 0;
	}
	
	public void add(ClassEntry child) {
		classes += child.classes;
		methods += child.methods;
		instances += child.instances;
		equals += child.equals;
		hashes += child.hashes;
		
		update();
	}
	
	private void update() {
		setField(classLabel, classes);
		setField(methodsLabel, methods);
		setField(instancesLabel, instances);
		setField(equalsLabel, equals);
		setField(hashLabel, hashes);
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return addDomHandler(handler, ClickEvent.getType());
	}
	
	private void setField(Label field, String value) {
		field.setText(value);
		field.setTitle(value);
	}
	
	private void setField(Label field, int value) {
		if (value != 0) {
			field.setText(String.valueOf(value));
		}
	}
}
