package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.Date;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;

import nz.ac.vuw.ecs.rprofs.client.request.id.DatasetIdProxy;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

@ProxyFor(value = Dataset.class, locator = DatasetManager.class)
public interface DatasetProxy extends EntityProxy {

	DatasetIdProxy getId();

	String getBenchmark();

	String getDatasetHandle();

	Date getStarted();

	Date getStopped();

	EntityProxyId<DatasetProxy> stableId();
}
