package nz.ac.vuw.ecs.rprofs.client.views;

import com.google.gwt.user.client.ui.IsWidget;
import nz.ac.vuw.ecs.rprofs.client.request.ClazzProxy;
import nz.ac.vuw.ecs.rprofs.client.request.FieldProxy;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.request.MethodProxy;

import java.util.List;

public interface ReportView extends IsWidget {

	public void setPresenter(Presenter presenter);

	public void showPackages(List<String> packages);

	public void showClasses(Object parent, List<? extends ClazzProxy> classes);

	public void showMethods(Object parent, List<? extends MethodProxy> methods);

	public void showFields(Object parent, List<? extends FieldProxy> fields);

	public void showInstances(Object parent, List<? extends InstanceProxy> instances);

	public void clearAll();

	public void clear(Object parent);

	public interface Presenter {
		public void selectPackage(String pkg);

		public void selectClass(ClazzProxy cls);

		public void selectMethod(MethodProxy method);

		public void selectField(FieldProxy field);

		public void selectInstance(InstanceProxy instance);
	}
}
