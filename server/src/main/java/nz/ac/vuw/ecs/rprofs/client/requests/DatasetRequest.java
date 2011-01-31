package nz.ac.vuw.ecs.rprofs.client.requests;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

import com.google.gwt.requestfactory.shared.InstanceRequest;
import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(Dataset.class)
public interface DatasetRequest extends RequestContext {

	Request<DatasetProxy> findDataset(String handle);
	Request<List<DatasetProxy>> findAllDatasets();
	Request<Void> stopDataset(DatasetProxy dataset);
	Request<Void> deleteDataset(DatasetProxy dataset);

	InstanceRequest<DatasetProxy, List<PackageProxy>> findPackages();
	InstanceRequest<DatasetProxy, List<ClassProxy>> findClasses(String pkg);
	InstanceRequest<DatasetProxy, List<MethodProxy>> findMethods(int cls);
	InstanceRequest<DatasetProxy, List<FieldProxy>> findFields(int cls);
	InstanceRequest<DatasetProxy, List<InstanceProxy>> findInstances(int cls);
	InstanceRequest<DatasetProxy, InstanceProxy> findInstance(long id);

	InstanceRequest<DatasetProxy, List<EventProxy>> findEventsByInstance(long id);

}
