package nz.ac.vuw.ecs.rprofs.client.activity;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.ReportPlace;
import nz.ac.vuw.ecs.rprofs.client.requests.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.requests.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.MethodProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.PackageProxy;
import nz.ac.vuw.ecs.rprofs.client.views.ReportView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.requestfactory.shared.Receiver;

public abstract class ReportActivity<T extends ReportPlace<T>> extends AbstractActivity implements ReportView.Presenter {

	private final ProfilerFactory factory;
	private final T place;
	private final DatasetProxy dataset;

	public ReportActivity(ProfilerFactory factory, T place) {
		this.factory = factory;
		this.place = place;

		if (place.getDatasetPlace() != null) {
			this.dataset = place.getDatasetPlace().getDataset();
		}
		else {
			this.dataset = null;
		}
	}

	public T getPlace() {
		return place;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(this.getClass() == obj.getClass())) return false;
		return place.equals(((ReportActivity<?>) obj).place);
	}

	@Override
	public int hashCode() {
		return place.hashCode();
	}

	protected void findPackages() {
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.findPackages().using(dataset).fire(new Receiver<List<PackageProxy>>() {
			@Override
			public void onSuccess(List<PackageProxy> response) {
				packagesAvailable(response);
			}
		});
	}

	protected void packagesAvailable(List<PackageProxy> packages) {}

	@Override
	public void selectPackage(PackageProxy pkg) {}

	protected void findClassesByPackage(final PackageProxy pkg) {
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.findClasses(pkg.getName()).using(dataset).fire(new Receiver<List<ClassProxy>>() {
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
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.findFields(cls.getIndex()).using(dataset).with("owner").fire(new Receiver<List<FieldProxy>>() {
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
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.findMethods(cls.getIndex()).using(dataset).with("owner").fire(new Receiver<List<MethodProxy>>() {
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
		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.findInstances(cls.getIndex()).using(dataset).with("type", "constructor").fire(new Receiver<List<InstanceProxy>>() {
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
