package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.DatasetService;
import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value=DatasetService.class, locator=ServiceLocator.class)
public interface DatasetRequest extends RequestContext {

	Request<DatasetProxy> findDataset(String handle);
	Request<List<DatasetProxy>> findAllDatasets();
	Request<Void> stopDataset(DatasetProxy dataset);
	Request<Void> deleteDataset(DatasetProxy dataset);

	Request<List<PackageProxy>> findPackages(DatasetProxy dataset);

	/*
	InstanceRequest<DatasetProxy, List<PackageProxy>> findPackages();
	InstanceRequest<DatasetProxy, List<ClassProxy>> findClasses(String pkg);
	InstanceRequest<DatasetProxy, List<MethodProxy>> findMethods(long cls);
	InstanceRequest<DatasetProxy, List<FieldProxy>> findFields(long cls);
	InstanceRequest<DatasetProxy, List<InstanceProxy>> findInstances(long cls);

	InstanceRequest<DatasetProxy, Integer> findNumEvents();
	InstanceRequest<DatasetProxy, List<EventProxy>> findEvents(int start, int limit);
	InstanceRequest<DatasetProxy, List<EventProxy>> findEventsByInstance(long id);
	 */
}
