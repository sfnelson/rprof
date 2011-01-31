package nz.ac.vuw.ecs.rprofs.client.activity;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.FieldBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.requests.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.PackageProxy;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class FieldBrowserActivity extends ReportActivity<FieldBrowserPlace>
implements ReportView.Presenter {

	private final ProfilerFactory factory;

	private ReportView view;

	public FieldBrowserActivity(ProfilerFactory factory, FieldBrowserPlace place) {
		super(factory, place);

		this.factory = factory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = factory.getReportView();
		panel.setWidget(view);

		view.setPresenter(this);

		findPackages();
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
		findFieldsByClass(cls);
	}

	@Override
	protected void fieldsAvailable(ClassProxy cls, List<FieldProxy> fields) {
		view.clear(cls);
		view.showFields(cls, fields);
	}
}
