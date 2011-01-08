package nz.ac.vuw.ecs.rprofs.client.views.impl;

import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.requests.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.MethodProxy;
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
	}

	@Override
	public void showPackages(List<String> packages) {
		clear();

		for (String pkg: packages) {
			ReportWidget w = createWidget(pkg);
			children.add(w);
			this.packages.add(w);
		}
	}

	@Override
	public void showClasses(List<ClassProxy> classes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showMethods(List<MethodProxy> methods) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showFields(List<FieldProxy> fields) {
		// TODO Auto-generated method stub

	}

	public void clear() {
		for (ReportWidget w: packages) {
			remove(w);
		}

		packages.clear();
	}

	void select(ReportWidget w) {
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
		for (ReportWidget c: w.remove()) {
			remove(c);
		}

		Object o = widgetMap.remove(w);
		objectMap.remove(o);
		available.add(w);
	}
}
