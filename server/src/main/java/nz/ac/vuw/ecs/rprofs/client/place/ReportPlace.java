package nz.ac.vuw.ecs.rprofs.client.place;

import com.google.gwt.place.shared.Place;

public abstract class ReportPlace<T extends ReportPlace<T>> extends Place {

	private static class NoReport extends ReportPlace<NoReport> {

		public NoReport(String handle, DatasetPlace dataset) {
			super(handle, dataset);
		}

		@Override
		public NoReport setDataset(DatasetPlace place) {
			return new NoReport(null, place);
		}

	}

	public static final NoReport NO_REPORT = new NoReport(null, null);

	private final String handle;
	private final DatasetPlace dataset;

	public ReportPlace(String handle, DatasetPlace dataset) {
		this.handle = handle;
		this.dataset = dataset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
		result = prime * result + ((handle == null) ? 0 : handle.hashCode());
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
		ReportPlace<?> other = (ReportPlace<?>) obj;
		if (dataset == null) {
			if (other.dataset != null)
				return false;
		} else if (!dataset.equals(other.dataset))
			return false;
		if (handle == null) {
			if (other.handle != null)
				return false;
		} else if (!handle.equals(other.handle))
			return false;
		return true;
	}


	public String getHandle() {
		return handle;
	}

	public DatasetPlace getDatasetPlace() {
		return dataset;
	}

	public abstract T setDataset(DatasetPlace place);

}
