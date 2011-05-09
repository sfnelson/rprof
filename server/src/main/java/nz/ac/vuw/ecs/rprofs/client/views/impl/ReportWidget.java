package nz.ac.vuw.ecs.rprofs.client.views.impl;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.request.MethodProxy;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.client.ui.ActivePanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

public class ReportWidget extends Composite {

	public static final int MAX_WIDGETS = 10;

	private static ReportWidgetUiBinder uiBinder = GWT.create(ReportWidgetUiBinder.class);
	interface ReportWidgetUiBinder extends UiBinder<Widget, ReportWidget> {}
	interface Style extends CssResource {
		String even();
		String odd();
		String count();
		String text();
		String longer();
	}

	@UiField Style style;

	@UiField ActivePanel data;
	@UiField HasWidgets children;

	private List<HTML> fields;

	private ReportPanel parent;
	private List<ReportWidget> widgets;

	public ReportWidget(ReportPanel parent) {
		this.parent = parent;
		widgets = Collections.newList();

		initWidget(uiBinder.createAndBindUi(this));

		fields = Collections.newList();
		for (int i = 0; i < MAX_WIDGETS; i++) {
			HTML field = new InlineHTML();
			fields.add(field);
			data.add(field);
		}
	}

	private boolean even;
	private int index;

	public void init(boolean even, int index) {
		if (this.even) {
			data.removeStyleName(style.even());
		}
		else {
			data.removeStyleName(style.odd());
		}

		this.even = even;
		this.index = index;

		for (HTML w: fields) {
			w.setText("");
			w.setTitle("");
			w.setStyleName("");
		}

		if (even) {
			data.addStyleName(style.even());
		}
		else {
			data.addStyleName(style.odd());
		}
	}

	public boolean hasChildren() {
		return !widgets.isEmpty();
	}

	public int getIndex() {
		return index;
	}

	public boolean isEven() {
		return even;
	}

	public void setCount(int index, int value, String label) {
		HTML count = fields.get(index);
		count.setText(String.valueOf(value));
		count.setTitle(label);
		count.setStyleName(style.count());
	}

	public void setText(int index, String pkg) {
		HTML name = fields.get(index);
		if ("".equals(pkg)) {
			name.setHTML("<em>default</em>");
		}
		else {
			name.setText(pkg);
		}
		name.setTitle(pkg);
		name.setStyleName(style.longer());
	}

	public void setText(int index, ClassProxy cls) {
		HTML name = fields.get(index);
		name.setText(cls.getSimpleName());
		name.setTitle(cls.getName());
		name.setStyleName(style.text());
	}

	public void setText(int index, FieldProxy f) {
		HTML name = fields.get(index);
		HTML desc = fields.get(index);

		String field = f.getName();
		String description = f.getDescription();
		String parent = f.getOwner().getName();

		name.setText(field);
		desc.setText(description);

		name.setTitle(parent + "." + field + ":" + description);

		name.setStyleName(style.text());
		desc.setStyleName(style.longer());
	}

	public void setText(int index, MethodProxy m) {
		HTML name = fields.get(index);
		HTML desc = fields.get(index + 1);

		String method = m.getName();
		String description = m.getDescription();
		String parent = m.getOwner().getName();

		name.setText(method);
		desc.setText(description);
		name.setTitle(parent + "." + method + ":" + description);

		name.setStyleName(style.text());
		desc.setStyleName(style.longer());
	}

	public void setText(int index, InstanceProxy i) {
		HTML name = fields.get(index);

		String instance = i.getThreadIndex() + "." + i.getInstanceIndex();
		String type = null;
		String shortType = null;
		if (i.getType() != null) {
			type = i.getType().getName();
			shortType = i.getType().getSimpleName();
		}

		if (type != null) {
			name.setText(shortType + ":" + instance);
			name.setTitle(type + ":" + instance);
		}
		else {
			name.setText(instance);
			name.setTitle(instance);
		}

		name.setStyleName(style.text());
	}

	public void addChild(ReportWidget child) {
		widgets.add(child);
		children.add(child);
	}

	public List<ReportWidget> clear() {
		List<ReportWidget> widgets = this.widgets;
		this.widgets = Collections.newList();
		return widgets;
	}

	@UiHandler("data")
	public void onClick(ClickEvent ev) {
		parent.select(this);
	}
}
