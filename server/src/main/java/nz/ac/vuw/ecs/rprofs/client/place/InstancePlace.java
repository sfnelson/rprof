package nz.ac.vuw.ecs.rprofs.client.place;

import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;

import com.google.gwt.place.shared.Place;

public class InstancePlace extends Place {

	public static final InstancePlace NO_INSTANCE = new InstancePlace(null, null);

	private final InstanceProxy instance;
	private final DatasetPlace dataset;

	public InstancePlace(InstanceProxy instance, DatasetPlace dataset) {
		this.instance = instance;
		this.dataset = dataset;
	}

	public InstanceProxy getInstance() {
		return instance;
	}

	public DatasetPlace getDataset() {
		return dataset;
	}

	public InstancePlace setDataset(DatasetPlace dataset) {
		return new InstancePlace(instance, dataset);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
		result = prime * result
		+ ((instance == null) ? 0 : instance.hashCode());
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
		InstancePlace other = (InstancePlace) obj;
		if (dataset == null) {
			if (other.dataset != null)
				return false;
		} else if (!dataset.equals(other.dataset))
			return false;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (instance == null) {
			return "null";
		}

		return instance.getType().getName() + ":" + instance.getThreadIndex() + "." + instance.getInstanceIndex();
	}

}
