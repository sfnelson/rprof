package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import nz.ac.vuw.ecs.rprofs.client.request.id.ClazzIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.InstanceIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.MethodIdProxy;
import nz.ac.vuw.ecs.rprofs.server.data.DomainObjectLocator;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

@ProxyFor(value = Instance.class, locator = DomainObjectLocator.class)
public interface InstanceProxy extends EntityProxy {

	public InstanceIdProxy getId();

	public ClazzIdProxy getType();

	public MethodIdProxy getConstructor();

}
