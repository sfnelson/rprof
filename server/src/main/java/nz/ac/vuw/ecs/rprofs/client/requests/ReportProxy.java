package nz.ac.vuw.ecs.rprofs.client.requests;

import nz.ac.vuw.ecs.rprofs.server.reports.Report;

import com.google.gwt.requestfactory.shared.EntityProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;
import com.google.gwt.requestfactory.shared.ProxyFor;

@ProxyFor(Report.class)
public interface ReportProxy extends EntityProxy {

	String getTitle();
	String getReference();
	String getDescription();

	EntityProxyId<ReportProxy> stableId();
}
