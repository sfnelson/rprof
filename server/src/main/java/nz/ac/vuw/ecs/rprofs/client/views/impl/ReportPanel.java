package nz.ac.vuw.ecs.rprofs.client.views.impl;

import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.request.MethodProxy;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ReportPanel extends Composite implements ReportView {

	private static ReportPanelUiBinder uiBinder = GWT.create(ReportPanelUiBinder.class);
	interface ReportPanelUiBinder extends UiBinder<Widget, ReportPanel> {}
	interface Style extends CssResource {
		String even();
		String refresh();
	}

	private Presenter presenter;

	private List<ReportWidget> available = Collections.newList();
	private Map<ReportWidget, Object> widgetMap = Collections.newMap();
	private Map<Object, ReportWidget> objectMap = Collections.newMap();

	private List<ReportWidget> packages = Collections.newList();

	@UiField HasWidgets children;

	public ReportPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;

		clearAll();
	}

	@Override
	public void showPackages(List<String> packages) {
		int index = 0;
		boolean even = false;
		for (String pkg: packages) {
			ReportWidget w = createWidget(pkg);
			w.init(even, index);
			w.setText(index, pkg);
			w.setCount(index + 1, 0 /*pkg.getNumClasses()*/, "Classes");
			children.add(w);
			this.packages.add(w);
			even = !even;
		}
	}

	@Override
	public void showClasses(Object parent, List<ClassProxy> classes) {
		ReportWidget p = objectMap.get(parent);
		if (p == null || classes == null) return;

		int index = p.getIndex() + 1;
		boolean even = !p.isEven();
		for (ClassProxy c: classes) {
			ReportWidget w = createWidget(c);
			w.init(even, index);
			w.setText(index, c);
			// w.setCount(index + 1, c.getNumMethods(), "Methods");
			// w.setCount(index + 2, c.getNumFields(), "Fields");
			p.addChild(w);
			even = !even;
		}
	}

	@Override
	public void showMethods(Object parent, List<MethodProxy> methods) {
		ReportWidget p = objectMap.get(parent);
		if (p == null || methods == null) return;

		int index = p.getIndex() + 1;
		boolean even = !p.isEven();
		for (MethodProxy m: methods) {
			if (m == null) {
				System.out.println("null method");
				continue;
			}
			ReportWidget w = createWidget(m);
			w.init(even, index);
			w.setText(index, m);
			p.addChild(w);
			even = !even;
		}
	}

	@Override
	public void showFields(Object parent, List<FieldProxy> fields) {
		ReportWidget p = objectMap.get(parent);
		if (p == null || fields == null) return;

		int index = p.getIndex() + 1;
		boolean even = !p.isEven();
		for (FieldProxy f: fields) {
			if (f == null) {
				System.out.println("null field");
				continue;
			}
			ReportWidget w = createWidget(f);
			w.init(even, index);
			w.setText(index, f);
			p.addChild(w);
			even = !even;
		}
	}

	@Override
	public void showInstances(Object parent, List<InstanceProxy> instances) {
		ReportWidget p = objectMap.get(parent);
		if (p == null || instances == null) return;

		int index = p.getIndex() + 1;
		boolean even = !p.isEven();
		for (InstanceProxy i: instances) {
			ReportWidget w = createWidget(i);
			w.init(even, index);
			w.setText(index, i);
			p.addChild(w);
			even = !even;
		}
	}

	public void clearAll() {
		remove(packages);
		packages.clear();
	}

	public void clear(Object parent) {
		ReportWidget p = objectMap.get(parent);
		if (p == null) return;

		remove(p.clear());
	}

	void select(ReportWidget w) {
		if (w.hasChildren()) {
			remove(w.clear());
			return;
		}

		Object o = widgetMap.get(w);
		if (o instanceof String) {
			presenter.selectPackage((String) o);
		}
		else if (o instanceof ClassProxy) {
			presenter.selectClass((ClassProxy) o);
		}
		else if (o instanceof FieldProxy) {
			presenter.selectField((FieldProxy) o);
		}
		else if (o instanceof MethodProxy) {
			presenter.selectMethod((MethodProxy) o);
		}
		else if (o instanceof InstanceProxy) {
			presenter.selectInstance((InstanceProxy) o);
		}
	}

	private ReportWidget createWidget(Object o) {
		ReportWidget w;
		if (available.isEmpty()) {
			w = new ReportWidget(this);
		}
		else {
			w = available.remove(available.size() - 1);
		}

		widgetMap.put(w, o);
		objectMap.put(o, w);

		return w;
	}

	private void remove(ReportWidget w) {
		remove(w.clear());

		Object o = widgetMap.remove(w);
		objectMap.remove(o);
		w.removeFromParent();
		available.add(w);
	}

	private void remove(List<ReportWidget> widgets) {
		for (ReportWidget c: widgets) {
			remove(c);
		}
	}
}
