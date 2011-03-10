package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Package;

import com.google.gwt.requestfactory.shared.ProxyFor;
import com.google.gwt.requestfactory.shared.ValueProxy;

@ProxyFor(value = Package.class)
public interface PackageProxy extends ValueProxy {

	public String getDataset();
	public String getName();
	public int getNumClasses();

}
