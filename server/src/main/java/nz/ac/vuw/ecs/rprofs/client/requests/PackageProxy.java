package nz.ac.vuw.ecs.rprofs.client.requests;

import nz.ac.vuw.ecs.rprofs.server.domain.Package;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(Package.class)
public interface PackageProxy extends EntityProxy {

	public String getName();
	public int getNumClasses();

}
