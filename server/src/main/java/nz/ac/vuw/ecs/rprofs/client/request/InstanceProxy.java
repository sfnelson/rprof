package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.data.DomainService.InstanceLocator;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(value = Instance.class, locator = InstanceLocator.class)
public interface InstanceProxy extends EntityProxy {

	public long getIndex();
	public short getThreadIndex();
	public int getInstanceIndex();

	public ClassProxy getType();
	public MethodProxy getConstructor();

	EntityProxyId<InstanceProxy> stableId();
}
