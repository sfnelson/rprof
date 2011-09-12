package nz.ac.vuw.ecs.rprofs.client.request;

public interface RequestFactory extends com.google.web.bindery.requestfactory.shared.RequestFactory {

	DatasetRequest datasetRequest();

	InstanceRequest instanceRequest();

	EventRequest eventRequest();

	ClazzRequest classRequest();

	FieldRequest fieldRequest();

	MethodRequest methodRequest();

}
