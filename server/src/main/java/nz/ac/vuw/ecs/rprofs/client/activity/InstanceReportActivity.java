package nz.ac.vuw.ecs.rprofs.client.activity;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.InstanceBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InstancePlace;
import nz.ac.vuw.ecs.rprofs.client.requests.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.PackageProxy;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class InstanceReportActivity extends ReportActivity<InstanceBrowserPlace>
implements ReportView.Presenter {

	private ProfilerFactory factory;
	private ReportView view;

	public InstanceReportActivity(ProfilerFactory factory, InstanceBrowserPlace place) {
		super(factory, place);
		this.factory = factory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = factory.getReportView();
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
		factory.getPlaceController().goTo(newPlace);
	}
}
