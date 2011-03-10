package nz.ac.vuw.ecs.rprofs.client.views;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.request.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.request.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.request.MethodProxy;
import nz.ac.vuw.ecs.rprofs.client.request.PackageProxy;

import com.google.gwt.user.client.ui.IsWidget;

public interface ReportView extends IsWidget {

	public void setPresenter(Presenter presenter);
	public void showPackages(List<PackageProxy> packages);
	public void showClasses(Object parent, List<ClassProxy> classes);
	public void showMethods(Object parent, List<MethodProxy> methods);
	public void showFields(Object parent, List<FieldProxy> fields);
	public void showInstances(Object parent, List<InstanceProxy> instances);

	public void clearAll();
	public void clear(Object parent);

	public interface Presenter {
		public void selectPackage(PackageProxy pkg);
		public void selectClass(ClassProxy cls);
		public void selectMethod(MethodProxy method);
		public void selectField(FieldProxy field);
		public void selectInstance(InstanceProxy instance);
	}
}
