package nz.ac.vuw.ecs.rprofs.server.domain.id;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StringId implements Serializable {

	private final String handle;

	public StringId(String id) {
		this.handle = id;
	}

	public String getHandle() {
		return handle;
	}

	@Override
	public int hashCode() {
		return handle.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (getClass() != o.getClass()) return false;
		StringId other = (StringId) o;
		if (handle == null) {
			if (other.handle != null)
				return false;
		} else if (!handle.equals(other.handle))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return handle;
	}

}
