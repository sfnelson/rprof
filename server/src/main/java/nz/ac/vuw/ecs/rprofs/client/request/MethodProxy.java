package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.data.MethodManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;

@ProxyFor(value = Method.class, locator = MethodManager.class)
public interface MethodProxy extends EntityProxy {

	public String getName();
	public String getDescription();
	public int getAccess();
	public ClassProxy getOwner();

	EntityProxyId<MethodProxy> stableId();
}
