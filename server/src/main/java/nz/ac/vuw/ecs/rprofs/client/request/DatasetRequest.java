package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;

@Service(value=DatasetService.class, locator=ServiceLocator.class)
public interface DatasetRequest extends RequestContext {
	Request<DatasetProxy> findDataset(String handle);
	Request<List<DatasetProxy>> findAllDatasets();
	Request<Void> stopDataset(String dataset);
	Request<Void> deleteDataset(String dataset);
}
