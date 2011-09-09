package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.user.client.ui.InlineLabel;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;

public class InstanceLabel extends InlineLabel {
	public void setInstance(InstanceProxy instance) {
		if (instance == null) {
			setText("null");
		} else {
			int upper = instance.getThreadIndex();
			int lower = instance.getInstanceIndex();

			setText(upper + "." + lower);
		}
	}
}
