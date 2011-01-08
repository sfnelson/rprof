package nz.ac.vuw.ecs.rprofs.client.views;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.requests.ClassProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.requests.MethodProxy;

import com.google.gwt.user.client.ui.IsWidget;

public interface ReportView extends IsWidget {

	public void setPresenter(Presenter presenter);
	public void showPackages(List<String> packages);
	public void showClasses(List<ClassProxy> classes);
	public void showMethods(List<MethodProxy> methods);
	public void showFields(List<FieldProxy> fields);
	public void clear();

	public interface Presenter {
		public void selectPackage(String pkg);
		public void selectClass(ClassProxy cls);
		public void selectMethod(MethodProxy method);
		public void selectField(FieldProxy field);
		public void selectInstance(InstanceProxy instance);
	}
}
