package nz.ac.vuw.ecs.rprofs.server.request;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class ServiceLocator implements com.google.web.bindery.requestfactory.shared.ServiceLocator {

	private final Injector injector;

	@Inject
	ServiceLocator(Injector injector) {
		this.injector = injector;
	}

	@Override
	public Object getInstance(Class<?> clazz) {
		return injector.getInstance(clazz);
	}

}
