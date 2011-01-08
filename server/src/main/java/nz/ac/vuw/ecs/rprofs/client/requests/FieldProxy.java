package nz.ac.vuw.ecs.rprofs.client.requests;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(Field.class)
public interface FieldProxy extends EntityProxy {

	public ClassProxy getOwner();
	public String getName();
	public String getDescription();
	public int getAccess();
	public boolean getEquals();
	public boolean getHash();

	EntityProxyId<FieldProxy> stableId();
}
