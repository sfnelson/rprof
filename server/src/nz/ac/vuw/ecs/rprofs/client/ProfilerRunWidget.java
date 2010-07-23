package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class ProfilerRunWidget extends Composite {
	
	private static ProfilerRunWidgetUiBinder uiBinder = GWT
			.create(ProfilerRunWidgetUiBinder.class);

	interface ProfilerRunWidgetUiBinder extends
			UiBinder<Widget, ProfilerRunWidget> {
	}
	
	private ProfilerRunsPane parent;
	private ProfilerRun run;

	@UiField InlineLabel program;
	@UiField InlineLabel started;
	@UiField InlineLabel stopped;
	@UiField Button inspect;
	@UiField Button delete;

	public ProfilerRunWidget(ProfilerRunsPane parent, ProfilerRun run) {
		initWidget(uiBinder.createAndBindUi(this));

		this.parent = parent;
		this.run = run;
		
		started.setText(run.started.toString());
		if (run.stopped != null) stopped.setText(run.stopped.toString());
		if (run.program != null) program.setText(run.program);
	}

	@UiHandler("inspect")
	void inspectClicked(ClickEvent e) {
		Rprof_server.getInstance().setProfilerRun(run);
		parent.select(this);
	}
	
	@UiHandler("delete")
	void onClick(ClickEvent e) {
		Rprof_server.getInstance().dropProfilerRun(run);
	}

}
