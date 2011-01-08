package nz.ac.vuw.ecs.rprofs.client.events;

import nz.ac.vuw.ecs.rprofs.client.events.DatasetEvent.DatasetHandler;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class DatasetEvent extends GwtEvent<DatasetHandler> {

	private static final Type<DatasetHandler> TYPE = new Type<DatasetEvent.DatasetHandler>();

	public static Type<DatasetHandler> getType() {
		return TYPE;
	}

	private final DatasetProxy dataset;

	public DatasetEvent(DatasetProxy dataset) {
		this.dataset = dataset;
	}

	@Override
	public Type<DatasetHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(DatasetHandler handler) {
		handler.datasetSelected(dataset);
	}

	public interface DatasetHandler extends EventHandler {
		public void datasetSelected(DatasetProxy dataset);
	}
}
