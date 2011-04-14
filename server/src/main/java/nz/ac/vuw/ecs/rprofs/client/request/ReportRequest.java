package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.reports.Report;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value = Report.class)
public interface ReportRequest extends RequestContext {

	Request<DatasetReportProxy> getDatasetReport(String datasetHandle);

}
