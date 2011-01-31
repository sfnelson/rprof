package nz.ac.vuw.ecs.rprofs.server.domain.id;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;


@SuppressWarnings("serial")
@MappedSuperclass
public class IntIntId implements Serializable {

	private int owner_index;
	private int index;

	public IntIntId() {}

	public IntIntId(int owner_index, int index) {
		this.owner_index = owner_index;
		this.index = index;
	}

	public int getOwnerIndex() {
		return owner_index;
	}

	public int getIndex() {
		return index;
	}

	public long getId() {
		return (((long) owner_index) << 32) | index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + owner_index;
		result = prime * result + index;
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
		IntIntId other = (IntIntId) obj;
		if (owner_index != other.owner_index)
			return false;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%d.%d", owner_index, index);
	}
}
