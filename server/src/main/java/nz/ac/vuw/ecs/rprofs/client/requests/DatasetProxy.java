package nz.ac.vuw.ecs.rprofs.client.requests;

import java.util.Date;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(Dataset.class)
public interface DatasetProxy extends EntityProxy {

	String getHandle();
	String getProgram();
	Date getStarted();
	Date getStopped();

	EntityProxyId<DatasetProxy> stableId();
}
