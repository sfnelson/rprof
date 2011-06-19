package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.data.FieldManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;

@ProxyFor(value = Field.class, locator = FieldManager.class)
public interface FieldProxy extends EntityProxy {

	public ClassProxy getOwner();
	public String getName();
	public String getDescription();
	public int getAccess();

	EntityProxyId<FieldProxy> stableId();
}
