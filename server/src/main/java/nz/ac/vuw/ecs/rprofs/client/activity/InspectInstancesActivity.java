package nz.ac.vuw.ecs.rprofs.client.activity;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import nz.ac.vuw.ecs.rprofs.client.activity.shared.AbstractTypedInspectorActivity;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseInstances;
import nz.ac.vuw.ecs.rprofs.client.place.shared.CompositePlace;
import nz.ac.vuw.ecs.rprofs.client.request.*;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import java.util.List;

public class InspectInstancesActivity extends AbstractTypedInspectorActivity<BrowseInstances>
		implements ReportView.Presenter {

	private final ReportView view;
	private final PlaceController pc;

	@Inject
	public InspectInstancesActivity(Provider<ClazzRequest> cr,
									Provider<FieldRequest> fr,
									Provider<MethodRequest> mr,
									Provider<InstanceRequest> ir,
									ReportView view,
									PlaceController pc) {
		super(cr, fr, mr, ir);

		this.view = view;
		this.pc = pc;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view.setPresenter(this);

		panel.setWidget(view);

		view.clearAll();
		findPackages();
	}

	@Override
	public void onStop() {
		view.clearAll();
		super.onStop();
	}

	@Override
	protected void packagesAvailable(List<String> packages) {
		view.showPackages(packages);
	}

	@Override
	public void selectPackage(final String pkg) {
		view.clear(pkg);
		findClassesByPackage(pkg);
	}

	@Override
	protected void classesAvailable(String pkg, List<ClazzProxy> classes) {
		view.showClasses(pkg, classes);
	}

	@Override
	public void selectClass(final ClazzProxy cls) {
		view.clear(cls);
		findInstancesByClass(cls);
	}

	@Override
	protected void instancesAvailable(ClazzProxy cls, List<InstanceProxy> instances) {
		view.showInstances(cls, instances);
	}

	@Override
	public void selectInstance(InstanceProxy instance) {
		Place current = pc.getWhere();
		if (current instanceof CompositePlace) {
			CompositePlace<?> c = (CompositePlace<?>) current;
			c = c.clonePlace();
			c.setParameter("i", String.valueOf(instance.stableId()));
			pc.goTo(c);
		}
	}
}
