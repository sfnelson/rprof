package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.data.InstanceManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;

@ProxyFor(value = Instance.class, locator = InstanceManager.class)
public interface InstanceProxy extends EntityProxy {

	public long getIndex();
	public short getThreadIndex();
	public int getInstanceIndex();

	public ClassProxy getType();
	public MethodProxy getConstructor();

	EntityProxyId<InstanceProxy> stableId();
}
