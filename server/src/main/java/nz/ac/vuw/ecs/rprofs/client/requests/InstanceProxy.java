package nz.ac.vuw.ecs.rprofs.client.requests;

import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(Instance.class)
public interface InstanceProxy extends EntityProxy {

	public ClassProxy getType();
	public MethodProxy getConstructor();

	EntityProxyId<InstanceProxy> stableId();
}
