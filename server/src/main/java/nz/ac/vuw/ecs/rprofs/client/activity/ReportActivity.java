package nz.ac.vuw.ecs.rprofs.client.activity;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.ReportPlace;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetProxy;

import com.google.gwt.activity.shared.AbstractActivity;

public abstract class ReportActivity<T extends ReportPlace<T>> extends AbstractActivity {

	private final ProfilerFactory factory;
	private final T place;
	private final DatasetProxy dataset;

	public ReportActivity(ProfilerFactory factory, T place) {
		this.factory = factory;
		this.place = place;

		if (place.getDatasetPlace() != null) {
			this.dataset = place.getDatasetPlace().getDataset();
		}
		else {
			this.dataset = null;
		}
	}

	protected ProfilerFactory getFactory() {
		return factory;
	}

	protected T getPlace() {
		return place;
	}

	protected DatasetProxy getDataset() {
		return dataset;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(this.getClass() == obj.getClass())) return false;
		return place.equals(((ReportActivity<?>) obj).place);
	}

	@Override
	public int hashCode() {
		return place.hashCode();
	}

}
