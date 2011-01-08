package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.Id;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventId implements IsSerializable, Comparable<EventId> {

	@Id
	long index;

	public EventId() {}

	public EventId(long index) {
		this.index = index;
	}

	@Override
	public int hashCode() {
		return new Long(index).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (!getClass().equals(o.getClass())) return false;
		EventId e = (EventId) o;
		return index == e.index;
	}

	@Override
	public int compareTo(EventId o) {
		if (index == o.index) return 0;
		return index < o.index ? -1 : 1;
	}
}
