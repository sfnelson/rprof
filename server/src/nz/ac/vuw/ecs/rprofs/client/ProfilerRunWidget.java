package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class ProfilerRunWidget extends Composite {
	
	private static final DateTimeFormat date = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
	
	private static ProfilerRunWidgetUiBinder uiBinder = GWT
			.create(ProfilerRunWidgetUiBinder.class);

	interface ProfilerRunWidgetUiBinder extends
			UiBinder<Widget, ProfilerRunWidget> {
	}
	
	private ProfilerRunsPane parent;
	ProfilerRun run;

	@UiField InlineLabel program;
	@UiField InlineLabel started;
	@UiField InlineLabel stopped;
	@UiField Button inspect;
	@UiField Button stop;
	@UiField Button delete;

	public ProfilerRunWidget(ProfilerRunsPane parent, ProfilerRun run) {
		initWidget(uiBinder.createAndBindUi(this));

		this.parent = parent;

		started.setText(date.format(run.started));
		update(run);
	}

	@UiHandler("inspect")
	void inspectClicked(ClickEvent e) {
		Inspector.getInstance().setProfilerRun(run);
		parent.select(this);
	}
	
	@UiHandler("stop")
	void stopClicked(ClickEvent e) {
		Inspector.getInstance().stopProfilerRun(run);
	}
	
	@UiHandler("delete")
	void onClick(ClickEvent e) {
		Inspector.getInstance().dropProfilerRun(run);
	}

	public void update(ProfilerRun run) {
		this.run = run;
		if (run.stopped != null) {
			stopped.setText(date.format(run.stopped));
			stop.setVisible(false);
			inspect.setVisible(true);
		}
		else {
			stop.setVisible(true);
			inspect.setVisible(false);
		}
		if (run.program != null) program.setText(run.program);
	}
}
