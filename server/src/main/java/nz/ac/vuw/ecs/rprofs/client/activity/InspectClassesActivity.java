package nz.ac.vuw.ecs.rprofs.client.activity;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import nz.ac.vuw.ecs.rprofs.client.activity.shared.AbstractTypedInspectorActivity;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseClasses;
import nz.ac.vuw.ecs.rprofs.client.request.*;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import java.util.List;

public class InspectClassesActivity
		extends AbstractTypedInspectorActivity<BrowseClasses>
		implements ReportView.Presenter {

	private final ReportView view;

	@Inject
	public InspectClassesActivity(Provider<ClazzRequest> cr,
								  Provider<FieldRequest> fr,
								  Provider<MethodRequest> mr,
								  Provider<InstanceRequest> ir,
								  ReportView view) {
		super(cr, fr, mr, ir);
		this.view = view;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view.setPresenter(this);

		panel.setWidget(view);

		findPackages();
	}

	@Override
	public void onStop() {
		super.onStop();
		view.clearAll();
	}

	@Override
	protected void packagesAvailable(List<String> packages) {
		view.clearAll();
		view.showPackages(packages);
	}

	@Override
	public void selectPackage(String pkg) {
		findClassesByPackage(pkg);
	}

	@Override
	protected void classesAvailable(String pkg, List<? extends ClazzProxy> classes) {
		view.clear(pkg);
		view.showClasses(pkg, classes);
	}

	@Override
	public void selectClass(ClazzProxy cls) {
		view.clear(cls);

		findMethodsByClass(cls);
		findFieldsByClass(cls);
	}

	@Override
	protected void fieldsAvailable(ClazzProxy cls, List<? extends FieldProxy> fields) {
		view.showFields(cls, fields);
	}

	@Override
	protected void methodsAvailable(ClazzProxy cls, List<? extends MethodProxy> methods) {
		view.showMethods(cls, methods);
	}

}
