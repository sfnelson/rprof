package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public interface View {

	Widget getContentItem();
	Button getMenuButton();
	void refresh();
	void hide();

}
