package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.reports.ReportManager;
import nz.ac.vuw.ecs.rprofs.server.request.ClassService;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;
import nz.ac.vuw.ecs.rprofs.server.request.FieldService;
import nz.ac.vuw.ecs.rprofs.server.request.InstanceService;
import nz.ac.vuw.ecs.rprofs.server.request.MethodService;
import nz.ac.vuw.ecs.rprofs.server.request.ReportService;


public class ServiceLocator implements com.google.web.bindery.requestfactory.shared.ServiceLocator {

	private final ContextManager cm = ContextManager.getInstance();

	@Override
	public Object getInstance(Class<?> clazz) {
		if (clazz == ClassService.class) {
			return new ClassManager(cm);
		}
		else if (clazz == DatasetService.class) {
			return new DatasetManager();
		}
		else if (clazz == EventService.class) {
			return new EventManager(cm);
		}
		else if (clazz == FieldService.class) {
			return new FieldManager(cm);
		}
		else if (clazz == InstanceService.class) {
			return new InstanceManager(cm);
		}
		else if (clazz == MethodService.class) {
			return new MethodManager(cm);
		}
		else if (clazz == ReportService.class) {
			return new ReportManager();
		}

		throw new NullPointerException("Don't know how to create a " + clazz.getSimpleName());
	}

}
