package nz.ac.vuw.ecs.rprofs.client.activity.shared;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Factory;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlace;
import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.ClassRequest;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.request.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.request.FieldRequest;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceRequest;
import nz.ac.vuw.ecs.rprofs.client.request.MethodProxy;
import nz.ac.vuw.ecs.rprofs.client.request.MethodRequest;
import nz.ac.vuw.ecs.rprofs.client.request.PackageProxy;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.requestfactory.shared.Receiver;

public abstract class AbstractTypedInspectorActivity<T extends ReportPlace> extends AbstractInspectorActivity<T> implements ReportView.Presenter {

	public AbstractTypedInspectorActivity(Factory factory, T place) {
		super(factory, place);
	}

	protected void findPackages() {
		System.out.println("requesting packages");
		DatasetRequest rq = getFactory().getRequestFactory().datasetRequest();
		rq.findPackages(getPlace().getDatasetHandle())
		.fire(new Receiver<List<PackageProxy>>() {
			@Override
			public void onSuccess(List<PackageProxy> response) {
				System.out.println("found packages");
				packagesAvailable(response);
			}
		});
	}

	protected void packagesAvailable(List<PackageProxy> packages) {}

	@Override
	public void selectPackage(PackageProxy pkg) {}

	protected void findClassesByPackage(final PackageProxy pkg) {
		ClassRequest rq = getFactory().getRequestFactory().classRequest();
		rq.findClasses(pkg.getDataset(), pkg.getName())
		.fire(new Receiver<List<ClassProxy>>() {
			@Override
			public void onSuccess(List<ClassProxy> response) {
				classesAvailable(pkg, response);
			}
		});
	}

	protected void classesAvailable(PackageProxy pkg, List<ClassProxy> classes) {}

	@Override
	public void selectClass(ClassProxy cls) {}

	protected void findFieldsByClass(final ClassProxy cls) {
		FieldRequest rq = getFactory().getRequestFactory().fieldRequest();
		rq.findFields(cls)
		.with("owner")
		.fire(new Receiver<List<FieldProxy>>() {
			@Override
			public void onSuccess(List<FieldProxy> response) {
				fieldsAvailable(cls, response);
			}
		});
	}

	protected void fieldsAvailable(ClassProxy cls, List<FieldProxy> fields) {}

	@Override
	public void selectField(FieldProxy field) {}

	protected void findMethodsByClass(final ClassProxy cls) {
		MethodRequest rq = getFactory().getRequestFactory().methodRequest();
		rq.findMethods(cls)
		.with("owner")
		.fire(new Receiver<List<MethodProxy>>() {
			@Override
			public void onSuccess(List<MethodProxy> response) {
				methodsAvailable(cls, response);
			}
		});
	}

	protected void methodsAvailable(ClassProxy cls, List<MethodProxy> methods) {}

	@Override
	public void selectMethod(MethodProxy method) {}

	protected void findInstancesByClass(final ClassProxy cls) {
		InstanceRequest rq = getFactory().getRequestFactory().instanceRequest();
		rq.findInstances(cls)
		.with("type", "constructor")
		.fire(new Receiver<List<InstanceProxy>>() {
			@Override
			public void onSuccess(List<InstanceProxy> response) {
				instancesAvailable(cls, response);
			}
		});
	}

	protected void instancesAvailable(ClassProxy cls, List<InstanceProxy> instances) {}

	@Override
	public void selectInstance(InstanceProxy instance) {}

}
