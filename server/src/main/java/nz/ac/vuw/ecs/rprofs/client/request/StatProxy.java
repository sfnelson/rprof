package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.ValueProxy;
import nz.ac.vuw.ecs.rprofs.server.reports.Stat;

@ProxyFor(Stat.class)
public interface StatProxy extends ValueProxy {
	float getMean();

	float getStdDev();
}
