package nz.ac.vuw.ecs.rprofs.client.requests;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(Class.class)
public interface ClassProxy extends EntityProxy {

	public int getIndex();

	public String getPackage();
	public String getName();
	public String getClassName();

	public ClassProxy getParent();
	public int getNumMethods();
	public int getNumFields();
	public int getProperties();

	EntityProxyId<ClassProxy> stableId();


}
