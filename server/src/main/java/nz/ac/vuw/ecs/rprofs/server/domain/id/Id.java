package nz.ac.vuw.ecs.rprofs.server.domain.id;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class Id<T extends DataObject<T, ?>> implements Serializable {

	private static final long mask = 0xFFFFFFFFl;

	private long id;

	public Id() {}

	public Id(long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return ((int) (id & mask)) ^ ((int) (id >>> 32));
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
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
