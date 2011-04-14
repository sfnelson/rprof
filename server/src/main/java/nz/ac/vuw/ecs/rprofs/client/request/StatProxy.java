package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.reports.Stat;

import com.google.gwt.requestfactory.shared.ProxyFor;
import com.google.gwt.requestfactory.shared.ValueProxy;

@ProxyFor(Stat.class)
public interface StatProxy extends ValueProxy {
	float getMean();
	float getStdDev();
}
