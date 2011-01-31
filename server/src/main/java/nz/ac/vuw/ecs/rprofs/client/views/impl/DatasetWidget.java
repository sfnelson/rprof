package nz.ac.vuw.ecs.rprofs.client.views.impl;

import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetView;

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

public class DatasetWidget extends Composite implements DatasetView, MouseOverHandler, MouseOutHandler {

	private static final DateTimeFormat date = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");

	private static DatasetWidgetUiBinder uiBinder = GWT
	.create(DatasetWidgetUiBinder.class);

	interface DatasetWidgetUiBinder extends UiBinder<Widget, DatasetWidget> {}

	DatasetProxy run;
	Presenter presenter;

	@UiField Anchor program;
	@UiField InlineLabel started;
	@UiField InlineLabel stopped;
	@UiField Button stop;
	@UiField Button delete;

	public DatasetWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		addDomHandler(this, MouseOverEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
	}

	@Override
	public void setDataset(DatasetProxy run) {
		this.run = run;

		started.setText(date.format(run.getStarted()));
		program.setTarget("#run="+run.getHandle());
		delete.setVisible(false);

		if (run.getStopped() != null) {
			stopped.setText(date.format(run.getStopped()));
			stop.setVisible(false);
		}
		else {
			stopped.setText("");
			stop.setVisible(true);
		}

		if (run.getProgram() != null) {
			program.setText(run.getProgram());
		}
		else {
			program.setText("");
		}
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public DatasetProxy getDataset() {
		return run;
	}

	@UiHandler("stop")
	void stopClicked(ClickEvent e) {
		presenter.stop(run);
	}

	@UiHandler("delete")
	void deleteClicked(ClickEvent e) {
		presenter.delete(run);
	}

	@UiHandler("program")
	void select(ClickEvent e) {
		if (e.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
			e.preventDefault();
			e.stopPropagation();
			presenter.select(run);
			program.setFocus(false);
		}
	}

	@UiHandler("program")
	void select(KeyPressEvent e) {
		if (e.getNativeEvent().getButton() == KeyCodes.KEY_ENTER) {
			e.preventDefault();
			e.stopPropagation();
			presenter.select(run);
		}
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		if (run.getStopped() != null) {
			delete.setVisible(true);
		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		delete.setVisible(false);
	}
}
