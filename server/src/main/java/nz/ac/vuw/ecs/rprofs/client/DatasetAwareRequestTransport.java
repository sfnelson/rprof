package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.gwt.client.DefaultRequestTransport;
import nz.ac.vuw.ecs.rprofs.client.place.shared.HasDataset;
import nz.ac.vuw.ecs.rprofs.client.request.id.DatasetIdProxy;

public class DatasetAwareRequestTransport extends DefaultRequestTransport implements PlaceChangeEvent.Handler {

	private DatasetIdProxy ds;

	@Inject
	public DatasetAwareRequestTransport(EventBus eventBus) {
		eventBus.addHandler(PlaceChangeEvent.TYPE, this);
	}

	@Override
	public String getRequestUrl() {
		if (ds != null) {
			return super.getRequestUrl() + "/" + ds.getValue();
		} else {
			return super.getRequestUrl();
		}
	}

	@Override
	public void onPlaceChange(PlaceChangeEvent event) {
		if (event.getNewPlace() instanceof HasDataset) {
			ds = ((HasDataset) event.getNewPlace()).getDatasetId();
		} else {
			ds = null;
		}
	}
}
