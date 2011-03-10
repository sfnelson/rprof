package nz.ac.vuw.ecs.rprofs.client.request;

public interface RequestFactory extends com.google.gwt.requestfactory.shared.RequestFactory {

	DatasetRequest datasetRequest();

	InstanceRequest instanceRequest();

	EventRequest eventRequest();

	ClassRequest classRequest();

	FieldRequest fieldRequest();

	MethodRequest methodRequest();

}
