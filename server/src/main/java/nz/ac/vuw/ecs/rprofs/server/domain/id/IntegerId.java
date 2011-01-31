package nz.ac.vuw.ecs.rprofs.server.domain.id;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;


@SuppressWarnings("serial")
@MappedSuperclass
public class IntegerId implements Serializable {

	private int index;

	public IntegerId() {}

	public IntegerId(int id) {
		this.index = id;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		IntegerId other = (IntegerId) obj;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.valueOf(index);
	}
}
