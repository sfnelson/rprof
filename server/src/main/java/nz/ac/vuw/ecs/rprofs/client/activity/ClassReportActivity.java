package nz.ac.vuw.ecs.rprofs.client.activity;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.ClassBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.request.MethodProxy;
import nz.ac.vuw.ecs.rprofs.client.request.PackageProxy;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ClassReportActivity extends TypeReportActivity<ClassBrowserPlace>
implements ReportView.Presenter {

	private ReportView view;

	public ClassReportActivity(ProfilerFactory factory, ClassBrowserPlace place) {
		super(factory, place);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = getFactory().getReportView();
		panel.setWidget(view);

		view.setPresenter(this);

		findPackages();
	}

	@Override
	public void onStop() {
		super.onStop();
		view.clearAll();
	}

	@Override
	protected void packagesAvailable(List<PackageProxy> packages) {
		view.clearAll();
		view.showPackages(packages);
	}

	@Override
	public void selectPackage(final PackageProxy pkg) {
		findClassesByPackage(pkg);
	}

	@Override
	protected void classesAvailable(PackageProxy pkg, List<ClassProxy> classes) {
		view.clear(pkg);
		view.showClasses(pkg, classes);
	}

	@Override
	public void selectClass(ClassProxy cls) {
		view.clear(cls);

		findMethodsByClass(cls);
		findFieldsByClass(cls);
	}

	@Override
	protected void fieldsAvailable(ClassProxy cls, List<FieldProxy> fields) {
		view.showFields(cls, fields);
	}

	@Override
	protected void methodsAvailable(ClassProxy cls, List<MethodProxy> methods) {
		view.showMethods(cls, methods);
	}

}
