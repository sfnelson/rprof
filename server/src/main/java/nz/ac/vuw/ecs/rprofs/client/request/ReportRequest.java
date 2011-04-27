package nz.ac.vuw.ecs.rprofs.client.request;

import nz.ac.vuw.ecs.rprofs.server.data.ServiceLocator;
import nz.ac.vuw.ecs.rprofs.server.request.ReportService;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value = ReportService.class, locator = ServiceLocator.class)
public interface ReportRequest extends RequestContext {

	Request<DatasetReportProxy> getDatasetReport();

}
