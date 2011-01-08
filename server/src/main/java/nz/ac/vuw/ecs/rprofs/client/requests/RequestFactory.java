package nz.ac.vuw.ecs.rprofs.client.requests;

public interface RequestFactory extends com.google.gwt.requestfactory.shared.RequestFactory {

	DatasetRequest datasetRequest();
	ReportRequest reportRequest();

}
