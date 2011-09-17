package nz.ac.vuw.ecs.rprofs.client.views.impl;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Provider;
import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
import nz.ac.vuw.ecs.rprofs.client.ui.PlaceAnchor;
import nz.ac.vuw.ecs.rprofs.client.ui.UIButton;
import nz.ac.vuw.ecs.rprofs.client.views.ViewListView;

import java.util.Map;

public class ReportSelectionPanel extends Composite implements ViewListView {

	public interface Templates extends SafeHtmlTemplates {
		@Template("Classes")
		SafeHtml classesCaption();

		@Template("Browse Classes.")
		SafeHtml classesTitle();

		@Template("Instances")
		SafeHtml instancesCaption();

		@Template("Browse Instances.")
		SafeHtml instancesTitle();

		@Template("Events")
		SafeHtml eventsCaption();

		@Template("Browse Events.")
		SafeHtml eventsTitle();

		@Template("Fields")
		SafeHtml fieldsCaption();

		@Template("Browse Fields.")
		SafeHtml fieldsTitle();
	}

	private static ReportSelectionPanelUiBinder uiBinder = GWT.create(ReportSelectionPanelUiBinder.class);

	interface ReportSelectionPanelUiBinder extends UiBinder<Widget, ReportSelectionPanel> {
	}

	interface Style extends CssResource {
		String selected();

		String menu();

		String content();

		String menuWrapper();

		String closed();

		String button();

		String closeButton();
	}

	private final Templates templates = GWT.create(Templates.class);

	@UiField
	Style style;

	@UiField
	Panel panel;
	@UiField
	Panel menu;
	@UiField
	SimplePanel content;

	@UiField
	UIButton close;

	private final Provider<FrameLayout> parent;

	private final Map<String, PlaceAnchor> buttons = Maps.newHashMap();

	private PlaceAnchor selected;

	private boolean closed = false;

	private Presenter presenter;

	@Inject
	public ReportSelectionPanel(Provider<FrameLayout> parent) {
		this.parent = parent;

		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSelected(String report) {
		if (selected != null) {
			selected.removeStyleName(style.selected());
		}

		selected = buttons.get(report);

		if (selected != null) {
			selected.addStyleName(style.selected());
		}
	}

	@Override
	public void addPlace(final String view, Provider<String> url) {
		SafeHtml html;
		String title;
		if (view.equals("classes")) {
			html = templates.classesCaption();
			title = templates.classesTitle().asString();
		} else if (view.equals("instances")) {
			html = templates.instancesCaption();
			title = templates.instancesTitle().asString();
		} else if (view.equals("fields")) {
			html = templates.fieldsCaption();
			title = templates.fieldsTitle().asString();
		} else if (view.equals("events")) {
			html = templates.eventsCaption();
			title = templates.eventsTitle().asString();
		} else {
			html = new SafeHtmlBuilder().appendEscaped(view).toSafeHtml();
			title = view;
		}

		PlaceAnchor anchor = new PlaceAnchor(url, html, title);
		anchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.selectView(view);
			}
		});
		anchor.setStyleName(style.button());
		buttons.put(view, anchor);
		menu.add(anchor);
	}

	@UiHandler("close")
	void closeClicked(ClickEvent e) {
		if (!closed) {
			parent.get().showTop(false);
			close.setHTML("<span>&raquo;</span>");
		} else {
			parent.get().showTop(true);
			close.setHTML("<span>&laquo;</span>");
		}
		closed = !closed;
	}

	@Override
	public void setWidget(IsWidget w) {
		content.setWidget(w);
	}
}
