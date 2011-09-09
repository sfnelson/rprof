package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import nz.ac.vuw.ecs.rprofs.server.data.DomainObjectLocator;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

@ProxyFor(value = Method.class, locator = DomainObjectLocator.class)
public interface MethodProxy extends EntityProxy {

	public String getName();

	public String getDescription();

	public int getAccess();

	public ClassProxy getOwner();

	EntityProxyId<MethodProxy> stableId();
}
