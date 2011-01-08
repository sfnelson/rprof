package nz.ac.vuw.ecs.rprofs.client.requests;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.reports.Report;

import com.google.gwt.requestfactory.shared.InstanceRequest;
import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(Report.class)
public interface ReportRequest extends RequestContext {

	Request<List<ReportProxy>> findAllReports();
	InstanceRequest<ReportProxy, ReportProxy> generateReport();
	InstanceRequest<ReportProxy, ReportProxy> updateReport();
}
