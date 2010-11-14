package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class ProfilerRunWidget extends Composite implements MouseOverHandler, MouseOutHandler {
	
	private static final DateTimeFormat date = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
	
	private static ProfilerRunWidgetUiBinder uiBinder = GWT
			.create(ProfilerRunWidgetUiBinder.class);

	interface ProfilerRunWidgetUiBinder extends
			UiBinder<Widget, ProfilerRunWidget> {
	}
	
	private ProfilerRunsPane parent;
	ProfilerRun run;

	@UiField Anchor program;
	@UiField InlineLabel started;
	@UiField InlineLabel stopped;
	@UiField Button stop;
	@UiField Button delete;

	public ProfilerRunWidget(ProfilerRunsPane parent, ProfilerRun run) {
		initWidget(uiBinder.createAndBindUi(this));
		addDomHandler(this, MouseOverEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());

		this.parent = parent;

		started.setText(date.format(run.started));
		program.setTarget("#run="+run.handle);
		delete.setVisible(false);
		
		update(run);
	}
	
	@UiHandler("stop")
	void stopClicked(ClickEvent e) {
		Inspector.getInstance().stopProfilerRun(run);
	}
	
	@UiHandler("delete")
	void deleteClicked(ClickEvent e) {
		Inspector.getInstance().dropProfilerRun(run);
	}
	
	@UiHandler("program")
	void select(ClickEvent e) {
		if (e.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
			e.preventDefault();
			e.stopPropagation();
			parent.select(this);
			program.setFocus(false);
		}
	}
	
	@UiHandler("program")
	void select(KeyPressEvent e) {
		if (e.getNativeEvent().getButton() == KeyCodes.KEY_ENTER) {
			e.preventDefault();
			e.stopPropagation();
			parent.select(this);
		}
	}
	
	public void update(ProfilerRun run) {
		this.run = run;
		if (run.stopped != null) {
			stopped.setText(date.format(run.stopped));
			stop.setVisible(false);
		}
		else {
			stop.setVisible(true);
		}
		if (run.program != null) {
			program.setText(run.program);
		}
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		if (run.stopped != null) {
			delete.setVisible(true);
		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		delete.setVisible(false);
	}
}
