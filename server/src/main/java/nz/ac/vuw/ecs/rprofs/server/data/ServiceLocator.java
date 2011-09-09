package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(autowire = Autowire.BY_TYPE)
public class ServiceLocator implements com.google.web.bindery.requestfactory.shared.ServiceLocator {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(ServiceLocator.class);

	@Autowired
	private DatasetManager datasets;

	@Autowired
	private EventManager events;

	@Override
	public Object getInstance(Class<?> clazz) {
		if (clazz == DatasetService.class) {
			return datasets;
		} else if (clazz == EventService.class) {
			return events;
		}

		log.warn("don't know how to locate a {}", clazz.getName());
		return null;
	}

}
