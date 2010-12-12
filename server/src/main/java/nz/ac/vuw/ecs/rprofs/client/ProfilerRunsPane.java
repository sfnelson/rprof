package nz.ac.vuw.ecs.rprofs.client;

import java.util.Iterator;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.RunData;
import nz.ac.vuw.ecs.rprofs.client.events.ProfilerRunHandler;
import nz.ac.vuw.ecs.rprofs.client.history.History;
import nz.ac.vuw.ecs.rprofs.client.history.HistoryManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProfilerRunsPane extends Composite implements Iterable<ProfilerRunWidget>,
ProfilerRunHandler, ValueChangeHandler<History> {

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

		HistoryManager.getInstance().addValueChangeHandler(this);

		root.addProfilerRunHandler(this);
	}

	public void profilerRunsAvailable(List<RunData> runs) {
		Iterator<ProfilerRunWidget> widget = iterator();
		Iterator<RunData> run = runs.iterator();

		if (run.hasNext()) {
			RunData current = run.next();
			while (current != null && widget.hasNext()) {
				ProfilerRunWidget w = widget.next();
				if (w.run.equals(current)) {
					w.update(current);
					current = run.hasNext() ? run.next() : null;
				}
				else {
					widget.remove();
				}
			}
			while (current != null) {
				panel.add(new ProfilerRunWidget(this, current));
				current = run.hasNext() ? run.next() : null;
			}
		}
		
		updateSelection(HistoryManager.getInstance().getHistory().run);
	}

	public void select(ProfilerRunWidget w) {
		History history = HistoryManager.getInstance().getHistory();
		history.run = w.run;
		HistoryManager.getInstance().update(history);
	}

	@Override
	public void onValueChange(ValueChangeEvent<History> event) {
		updateSelection(event.getValue().run);
	}
	
	private void updateSelection(RunData run) {
		if (selected != null) {
			selected.removeStyleName(style.selected());
		}

		if (run == null) return;

		for (ProfilerRunWidget w: this) {
			if (w.run.equals(run)) {
				w.addStyleName(style.selected());
				selected = w;
				return;
			}
		}
	}

	@Override
	public Iterator<ProfilerRunWidget> iterator() {
		return new Iterator<ProfilerRunWidget>() {
			private int i = -1;
			public boolean hasNext() {
				return (i + 1 < panel.getWidgetCount());
			}
			public ProfilerRunWidget next() {
				i++;
				return (ProfilerRunWidget) panel.getWidget(i);
			}
			public void remove() {
				if (i >= 0 && i < panel.getWidgetCount()) {
					panel.remove(i);
					i--;
				}
			}
		};
	}
}
