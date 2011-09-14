package nz.ac.vuw.ecs.rprofs.client.activity.shared;

import com.google.gwt.core.client.GWT;
import com.google.inject.Provider;
import com.google.web.bindery.requestfactory.shared.Receiver;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlace;
import nz.ac.vuw.ecs.rprofs.client.request.*;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import java.util.List;

public abstract class AbstractTypedInspectorActivity<T extends ReportPlace<T>>
		extends AbstractInspectorActivity<T>
		implements ReportView.Presenter {

	private final Provider<ClazzRequest> cr;
	private final Provider<FieldRequest> fr;
	private final Provider<MethodRequest> mr;
	private final Provider<InstanceRequest> ir;

	public AbstractTypedInspectorActivity(Provider<ClazzRequest> cr,
										  Provider<FieldRequest> fr,
										  Provider<MethodRequest> mr,
										  Provider<InstanceRequest> ir) {
		this.cr = cr;
		this.fr = fr;
		this.mr = mr;
		this.ir = ir;
	}

	protected void findPackages() {
		GWT.log("requesting packages");
		cr.get().findPackages().fire(new Receiver<List<String>>() {
			@Override
			public void onSuccess(List<String> response) {
				System.out.println("found packages");
				packagesAvailable(response);
			}
		});
	}

	protected void packagesAvailable(List<String> packages) {
	}

	@Override
	public void selectPackage(String pkg) {
	}

	protected void findClassesByPackage(final String pkg) {
		cr.get().findClasses(pkg)
				.fire(new Receiver<List<? extends ClazzProxy>>() {
					@Override
					public void onSuccess(List<? extends ClazzProxy> response) {
						classesAvailable(pkg, response);
					}
				});
	}

	protected void classesAvailable(String pkg, List<? extends ClazzProxy> classes) {
	}

	@Override
	public void selectClass(ClazzProxy cls) {
	}

	protected void findFieldsByClass(final ClazzProxy cls) {
		fr.get().findFields(cls.getId())
				.with("owner")
				.fire(new Receiver<List<? extends FieldProxy>>() {
					@Override
					public void onSuccess(List<? extends FieldProxy> response) {
						fieldsAvailable(cls, response);
					}
				});
	}

	protected void fieldsAvailable(ClazzProxy cls, List<? extends FieldProxy> fields) {
	}

	@Override
	public void selectField(FieldProxy field) {
	}

	protected void findMethodsByClass(final ClazzProxy cls) {
		mr.get().findMethods(cls.getId())
				.with("owner")
				.fire(new Receiver<List<? extends MethodProxy>>() {
					@Override
					public void onSuccess(List<? extends MethodProxy> response) {
						methodsAvailable(cls, response);
					}
				});
	}

	protected void methodsAvailable(ClazzProxy cls, List<? extends MethodProxy> methods) {
	}

	@Override
	public void selectMethod(MethodProxy method) {
	}

	protected void findInstancesByClass(final ClazzProxy cls) {
		ir.get().findInstancesForClass(cls)
				.with("type", "constructor")
				.fire(new Receiver<List<? extends InstanceProxy>>() {
					@Override
					public void onSuccess(List<? extends InstanceProxy> response) {
						instancesAvailable(cls, response);
					}
				});
	}

	protected void instancesAvailable(ClazzProxy cls, List<? extends InstanceProxy> instances) {
	}

	@Override
	public void selectInstance(InstanceProxy instance) {
	}

}
