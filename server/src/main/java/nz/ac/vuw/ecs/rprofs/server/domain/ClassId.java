package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.Id;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClassId implements IsSerializable, Comparable<ClassId> {

	@Id
	Integer index;

	String name;

	public ClassId() {}

	public ClassId(int index, String name) {
		this.index = index;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!getClass().equals(o.getClass())) return false;
		ClassId c = (ClassId) o;
		return index == c.index;
	}

	@Override
	public int hashCode() {
		return index;
	}

	public int compareTo(ClassId o) {
		return o.index - index;
	}
}
