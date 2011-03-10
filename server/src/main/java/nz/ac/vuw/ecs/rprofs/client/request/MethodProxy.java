package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.data.DomainService.MethodLocator;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(value = Method.class, locator = MethodLocator.class)
public interface MethodProxy extends EntityProxy {

	public String getName();
	public String getDescription();
	public int getAccess();
	public ClassProxy getOwner();

	EntityProxyId<MethodProxy> stableId();
}
