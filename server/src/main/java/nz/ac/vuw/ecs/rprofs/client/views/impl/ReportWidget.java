package nz.ac.vuw.ecs.rprofs.client.views.impl;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import nz.ac.vuw.ecs.rprofs.client.request.ClazzProxy;
import nz.ac.vuw.ecs.rprofs.client.request.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.request.MethodProxy;
import nz.ac.vuw.ecs.rprofs.client.ui.ActivePanel;

import java.util.List;

public class ReportWidget extends Composite {

	public static final int MAX_WIDGETS = 10;

	private static ReportWidgetUiBinder uiBinder = GWT.create(ReportWidgetUiBinder.class);

	interface ReportWidgetUiBinder extends UiBinder<Widget, ReportWidget> {
	}

	interface Style extends CssResource {
		String even();

		String odd();

		String count();

		String text();

		String longer();

		String data();

		String children();
	}

	@UiField
	Style style;

	@UiField
	ActivePanel data;
	@UiField
	HasWidgets children;

	private List<HTML> fields;

	private ReportPanel parent;
	private List<ReportWidget> widgets;

	public ReportWidget(ReportPanel parent) {
		this.parent = parent;
		widgets = Lists.newArrayList();

		initWidget(uiBinder.createAndBindUi(this));

		fields = Lists.newArrayList();
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
		} else {
			data.removeStyleName(style.odd());
		}

		this.even = even;
		this.index = index;

		for (HTML w : fields) {
			w.setText("");
			w.setTitle("");
			w.setStyleName("");
		}

		if (even) {
			data.addStyleName(style.even());
		} else {
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
		} else {
			name.setText(pkg);
		}
		name.setTitle(pkg);
		name.setStyleName(style.longer());
	}

	public void setText(int index, ClazzProxy cls) {
		HTML name = fields.get(index);
		name.setText(cls.getName());
		name.setTitle(cls.getName());
		name.setStyleName(style.text());
	}

	public void setText(int index, FieldProxy f) {
		HTML name = fields.get(index);
		HTML desc = fields.get(index);

		String field = f.getName();
		String description = f.getDescription();
		String parent = String.valueOf(f.getOwner());

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
		String parent = String.valueOf(m.getOwner());

		name.setText(method);
		desc.setText(description);
		name.setTitle(parent + "." + method + ":" + description);

		name.setStyleName(style.text());
		desc.setStyleName(style.longer());
	}

	public void setText(int index, InstanceProxy i) {
		HTML name = fields.get(index);

		String instance = String.valueOf(i);
		String type = null;
		String shortType = null;
		if (i.getType() != null) {
			type = String.valueOf(i.getType());
			shortType = String.valueOf(i.getType());
		}

		if (type != null) {
			name.setText(shortType + ":" + instance);
			name.setTitle(type + ":" + instance);
		} else {
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
		this.widgets = Lists.newArrayList();
		return widgets;
	}

	@UiHandler("data")
	public void onClick(ClickEvent ev) {
		parent.select(this);
	}
}
