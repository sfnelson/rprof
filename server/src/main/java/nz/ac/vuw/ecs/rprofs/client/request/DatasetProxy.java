package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import nz.ac.vuw.ecs.rprofs.server.data.DomainObjectLocator;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

import java.util.Date;

@ProxyFor(value = Dataset.class, locator = DomainObjectLocator.class)
public interface DatasetProxy extends EntityProxy {

	String getHandle();

	String getProgram();

	Date getStarted();

	Date getStopped();

	EntityProxyId<DatasetProxy> stableId();
}
