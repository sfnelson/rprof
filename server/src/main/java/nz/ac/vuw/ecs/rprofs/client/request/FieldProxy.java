package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import nz.ac.vuw.ecs.rprofs.server.data.DomainObjectLocator;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;

@ProxyFor(value = Field.class, locator = DomainObjectLocator.class)
public interface FieldProxy extends EntityProxy {

	public ClassProxy getOwner();

	public String getName();

	public String getDescription();

	public int getAccess();

	EntityProxyId<FieldProxy> stableId();
}
