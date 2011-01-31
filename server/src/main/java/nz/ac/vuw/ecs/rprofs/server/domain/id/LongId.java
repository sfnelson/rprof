package nz.ac.vuw.ecs.rprofs.server.domain.id;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;


@SuppressWarnings("serial")
@MappedSuperclass
public class LongId implements Serializable {

	private long index;

	public LongId() {}

	public LongId(long id) {
		this.index = id;
	}

	public long getIndex() {
		return index;
	}

	public int getUpper() {
		return (int) (index >>> 32);
	}

	public int getLower() {
		return (int) (index & 0xFFFFFFFFl);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (index ^ (index >>> 32));
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
		LongId other = (LongId) obj;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%d.%d", (int) (index >>> 32), (int) (index & 0xFFFFFFFFl));
	}
}
