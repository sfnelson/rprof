package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

abstract class Id<I extends Id<I, T>, T extends DataObject<I, T>>
		implements nz.ac.vuw.ecs.rprofs.server.model.Id<I, T> {

	private static final long mask = 0xFFFFFFFFl;

	private long value;

	public Id() {
	}

	public Id(long value) {
		this.value = value;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return ((int) (value & mask)) ^ ((int) (value >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Id<?, ?> other = (Id<?, ?>) obj;

		return value == other.value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
