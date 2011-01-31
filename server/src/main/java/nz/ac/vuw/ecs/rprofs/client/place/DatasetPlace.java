package nz.ac.vuw.ecs.rprofs.client.place;

import nz.ac.vuw.ecs.rprofs.client.requests.DatasetProxy;

import com.google.gwt.place.shared.Place;

public class DatasetPlace extends Place {

	public static final DatasetPlace NO_DATASET = new DatasetPlace(null);

	private final DatasetProxy dataset;

	public DatasetPlace(DatasetProxy dataset) {
		this.dataset = dataset;
	}

	public DatasetProxy getDataset() {
		return dataset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatasetPlace other = (DatasetPlace) obj;
		if (dataset == null) {
			if (other.dataset != null)
				return false;
		} else if (!dataset.equals(other.dataset))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (dataset == null) {
			return "";
		}
		else {
			return dataset.getHandle();
		}
	}

}
