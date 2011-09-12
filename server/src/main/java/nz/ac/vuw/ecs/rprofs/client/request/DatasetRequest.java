package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import nz.ac.vuw.ecs.rprofs.client.request.id.DatasetIdProxy;
import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;

import java.util.List;

@Service(value = DatasetService.class, locator = ServiceLocator.class)
public interface DatasetRequest extends RequestContext {

	Request<DatasetProxy> findDataset(String handle);

	Request<DatasetProxy> findDataset(DatasetIdProxy id);

	Request<List<DatasetProxy>> findAllDatasets();

	Request<Void> stopDataset(DatasetProxy dataset);

	Request<Void> deleteDataset(DatasetProxy dataset);
}
