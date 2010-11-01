package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class InspectorPanel extends Composite {

	private static InspectorPanelUiBinder uiBinder = GWT
	.create(InspectorPanelUiBinder.class);

	interface InspectorPanelUiBinder extends UiBinder<Widget, InspectorPanel> {
	}

	@UiField Style style;

	interface Style extends CssResource {
		String button();
		String selected();
		String closed();
	}

	@UiField Panel panel;
	@UiField Panel header;
	@UiField Panel menu;
	@UiField Panel content;

	@UiField FocusPanel close;
	@UiField HTML closeText;

	private View active;
	private boolean closed = false;

	public InspectorPanel(Widget header) {
		initWidget(uiBinder.createAndBindUi(this));

		this.header.add(header);

		closeText.setHTML("&laquo;");
	}

	public void addView(final View view) {
		Button b = view.getMenuButton();
		b.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				InspectorPanel.this.onClick(view);
			}
		});
		b.setStyleName(style.button());
		menu.add(b);

		if (active == null) {
			onClick(view);
		}
	}

	private void onClick(View view) {
		if (active == view) {
			return;
		}
		content.clear();
		content.add(view.getContentItem());
		if (active != null) {
			active.getMenuButton().removeStyleName(style.selected());
			active.hide();
		}
		active = view;
		active.getMenuButton().addStyleName(style.selected());
		active.refresh();
	}

	public void refresh() {
		if (active != null) {
			active.refresh();
		}
	}
	
	@UiHandler("close")
	void mouseDown(ClickEvent e) {
		if (!closed) {
			panel.addStyleName(style.closed());
			closeText.setHTML("&raquo;");
		} else {
			panel.removeStyleName(style.closed());
			closeText.setHTML("&laquo;");
		}
		closed = !closed;
	}
}
