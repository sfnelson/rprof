package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProfilerRunsPane extends Composite implements ProfilerRunListener {

	private static ProfilerRunsPaneUiBinder uiBinder = GWT
			.create(ProfilerRunsPaneUiBinder.class);

	interface ProfilerRunsPaneUiBinder extends
			UiBinder<Widget, ProfilerRunsPane> {
	}
	
	private Widget selected;
	
	@UiField Style style;

	interface Style extends CssResource {
		String selected();
	}

	@UiField FlowPanel panel;

	public ProfilerRunsPane(Inspector root) {
		initWidget(uiBinder.createAndBindUi(this));
		
		root.addProfilerRunListener(this);
	}

	public void profilerRunsAvailable(List<ProfilerRun> runs) {
		int i = 0;
		while (i < runs.size()) {
			ProfilerRun run = runs.get(i);
			if (i < panel.getWidgetCount()) {
				ProfilerRunWidget w = (ProfilerRunWidget) panel.getWidget(i);
				if (w.run.equals(run)) {
					w.update(run);
					i++;
				}
				else {
					panel.remove(w);
				}
			}
			else {
				panel.add(new ProfilerRunWidget(this, run));
			}
		}
		
		while (i < panel.getWidgetCount()) {
			panel.remove(i);
		}
	}

	public void select(Widget w) {
		if (w != selected) {
			if (selected != null) {
				selected.removeStyleName(style.selected());
			}
			selected = w;
			selected.addStyleName(style.selected());
		}
	}
}
