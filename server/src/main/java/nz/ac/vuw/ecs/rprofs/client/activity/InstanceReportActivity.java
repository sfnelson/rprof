package nz.ac.vuw.ecs.rprofs.client.activity;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.InstanceBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InstancePlace;
import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.request.PackageProxy;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class InstanceReportActivity extends TypeReportActivity<InstanceBrowserPlace>
implements ReportView.Presenter {

	private ReportView view;

	public InstanceReportActivity(ProfilerFactory factory, InstanceBrowserPlace place) {
		super(factory, place);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = getFactory().getReportView();
		panel.setWidget(view);

		view.setPresenter(this);
		view.clearAll();
		findPackages();
	}

	@Override
	public void onStop() {
		view.clearAll();
		super.onStop();
	}

	@Override
	protected void packagesAvailable(List<PackageProxy> packages) {
		view.showPackages(packages);
	}

	@Override
	public void selectPackage(final PackageProxy pkg) {
		view.clear(pkg);
		findClassesByPackage(pkg);
	}

	@Override
	protected void classesAvailable(PackageProxy pkg, List<ClassProxy> classes) {
		view.showClasses(pkg, classes);
	}

	@Override
	public void selectClass(final ClassProxy cls) {
		view.clear(cls);
		findInstancesByClass(cls);
	}

	@Override
	protected void instancesAvailable(ClassProxy cls, List<InstanceProxy> instances) {
		view.showInstances(cls, instances);
	}

	@Override
	public void selectInstance(InstanceProxy instance) {
		InstancePlace newPlace = new InstancePlace(instance, getPlace().getDatasetPlace());
		getFactory().getPlaceController().goTo(newPlace);
	}
}
