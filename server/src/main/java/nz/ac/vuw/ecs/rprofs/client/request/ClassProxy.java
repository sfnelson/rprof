package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.data.DomainService.ClassLocator;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(value = Class.class, locator = ClassLocator.class)
public interface ClassProxy extends EntityProxy {

	public String getPackage();
	public String getName();
	public String getClassName();

	public ClassProxy getParent();
	public int getNumMethods();
	public int getNumFields();
	public int getProperties();

	EntityProxyId<ClassProxy> stableId();

}
