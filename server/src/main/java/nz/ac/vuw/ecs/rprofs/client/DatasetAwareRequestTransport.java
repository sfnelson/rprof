package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.place.shared.HasDataset;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.requestfactory.client.DefaultRequestTransport;

public class DatasetAwareRequestTransport extends DefaultRequestTransport implements PlaceChangeEvent.Handler {

	private String ds;

	public DatasetAwareRequestTransport(EventBus eventBus) {
		eventBus.addHandler(PlaceChangeEvent.TYPE, this);
	}

	@Override
	public String getRequestUrl() {
		if (ds != null) {
			return super.getRequestUrl() + "/" + ds;
		}
		else {
			return super.getRequestUrl();
		}
	}

	@Override
	public void onPlaceChange(PlaceChangeEvent event) {
		if (event.getNewPlace() instanceof HasDataset) {
			ds = ((HasDataset) event.getNewPlace()).getDatasetHandle();
		}
		else {
			ds = null;
		}
	}
}
