package nz.ac.vuw.ecs.rprofs.client.activity;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Factory;
import nz.ac.vuw.ecs.rprofs.client.activity.shared.AbstractTypedInspectorActivity;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseFields;
import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class InspectFieldsActivity
extends AbstractTypedInspectorActivity<BrowseFields>
implements ReportView.Presenter {

	private ReportView view;

	public InspectFieldsActivity(Factory factory, BrowseFields place) {
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
	protected void packagesAvailable(List<String> packages) {
		view.clearAll();
		view.showPackages(packages);
	}

	@Override
	public void selectPackage(final String pkg) {
		findClassesByPackage(pkg);
	}

	@Override
	protected void classesAvailable(String pkg, List<ClassProxy> classes) {
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
