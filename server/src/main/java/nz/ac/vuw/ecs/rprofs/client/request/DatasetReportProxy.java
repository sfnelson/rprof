package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.reports.DatasetReport;

import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyFor(value=DatasetReport.class)
public interface DatasetReportProxy extends ValueProxy {
	int getNumClasses();
	int getNumObjects();
	StatProxy getObjectsPerClass();
	StatProxy getWritesPerClass();
	StatProxy getWritesPerObject();
	StatProxy getReadsPerClass();
	StatProxy getReadsPerObject();
}
