package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.logging.Logger;

import nz.ac.vuw.ecs.rprofs.server.request.ClassService;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;
import nz.ac.vuw.ecs.rprofs.server.request.FieldService;
import nz.ac.vuw.ecs.rprofs.server.request.InstanceService;
import nz.ac.vuw.ecs.rprofs.server.request.MethodService;
import nz.ac.vuw.ecs.rprofs.server.request.ReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class ServiceLocator implements com.google.web.bindery.requestfactory.shared.ServiceLocator {

	private final Logger log = Logger.getLogger("service-locator");

	@Autowired ClassService classes;
	@Autowired DatasetService datasets;
	@Autowired EventService events;
	@Autowired FieldService fields;
	@Autowired InstanceService instances;
	@Autowired MethodService methods;
	@Autowired ReportService reports;

	@Override
	public Object getInstance(Class<?> clazz) {
		if (clazz == ClassService.class) {
			return classes;
		}
		else if (clazz == DatasetService.class) {
			return datasets;
		}
		else if (clazz == EventService.class) {
			return events;
		}
		else if (clazz == FieldService.class) {
			return fields;
		}
		else if (clazz == InstanceService.class) {
			return instances;
		}
		else if (clazz == MethodService.class) {
			return methods;
		}
		else if (clazz == ReportService.class) {
			return reports;
		}

		log.warning("don't know how to locate a " + clazz.getName());
		return null;
	}

}
