package nz.ac.vuw.ecs.rprofs.client.place.shared;

import nz.ac.vuw.ecs.rprofs.client.Factory;
import nz.ac.vuw.ecs.rprofs.client.place.SelectDataset;
import nz.ac.vuw.ecs.rprofs.client.place.ShowDataset;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;

public class PlaceManager implements PlaceChangeEvent.Handler {

	private Place current;

	public PlaceManager(Factory factory) {
		factory.getEventBus().addHandler(PlaceChangeEvent.TYPE, this);
	}

	@Override
	public void onPlaceChange(PlaceChangeEvent event) {
		current = event.getNewPlace();
	}

	public Place getCurrent() {
		return current;
	}

	public Place setDataset(DatasetProxy dataset) {
		if (dataset == null) {
			return new SelectDataset();
		}
		else {
			return new ShowDataset(dataset.getHandle());
		}
	}
}
