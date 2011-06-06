package nz.ac.vuw.ecs.rprofs.client.request;

import java.util.Date;

import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(value = Dataset.class, locator = DatasetManager.class)
public interface DatasetProxy extends EntityProxy {

	String getHandle();
	String getProgram();
	Date getStarted();
	Date getStopped();

	EntityProxyId<DatasetProxy> stableId();
}