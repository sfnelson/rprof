package nz.ac.vuw.ecs.rprofs.server.domain.id;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

@SuppressWarnings("serial")
public abstract class Id<T extends DataObject<T, ?>> implements Serializable {

	private static final long mask = 0xFFFFFFFFl;

	private long value;

	public Id() {}

	public Id(long value) {
		this.value = value;
	}

	public Long longValue() {
		return value;
	}

	public void setValue(Long value) {
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

		Id<?> other = (Id<?>) obj;
		
		return value == other.value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
