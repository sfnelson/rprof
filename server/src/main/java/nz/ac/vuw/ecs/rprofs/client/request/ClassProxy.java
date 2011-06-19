package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;

@ProxyFor(value = Class.class, locator = ClassManager.class)
public interface ClassProxy extends EntityProxy {

	public String getPackage();
	public String getSimpleName();
	public String getName();

	public ClassProxy getParent();
	public int getProperties();

	EntityProxyId<ClassProxy> stableId();

}
