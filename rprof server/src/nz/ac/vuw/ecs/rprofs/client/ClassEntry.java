package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Panel;


public class ClassEntry extends Composite {
	
	private Panel container;
	
	public ClassEntry(ClassRecord cr) {
		container = new FlowPanel();
		initWidget(container);
		
		container.add(new InlineLabel(cr.name));
	}

}
