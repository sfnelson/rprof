/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.data.Report;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ProgressBar extends Composite {

	private static ProgressBarUiBinder uiBinder = GWT.create(ProgressBarUiBinder.class);
	interface ProgressBarUiBinder extends UiBinder<Widget, ProgressBar> {}

	@UiField HTML message;
	@UiField Widget meter;
	@UiField HTML progress;
	
	public ProgressBar() {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.setVisible(false);
	}
	
	public void update(Report.Status status) {
		switch (status.state) {
		case UNINITIALIZED:
		case GENERATING:
			message.setText(status.stage);
			if (status.limit != 0) {
				meter.setWidth((100.0f * status.progress / status.limit) + "%");
			}
			else {
				meter.setWidth("0");
			}
			progress.setText(status.progress + " / " + status.limit);
			this.setVisible(true);
			break;
		case READY:
			this.setVisible(false);
			break;
		}
	}
}
