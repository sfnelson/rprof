package nz.ac.vuw.ecs.rprofs.server.domain;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MethodId implements AttributeId, IsSerializable, Comparable<MethodId> {

	Method.PK key;

	ClassId owner;
	String name;

	public MethodId() {}

	public MethodId(ClassId owner, int index, String name) {
		this.key = new Method.PK();
		this.key.owner_index = owner.index;
		this.key.index = index;
		this.name = name;
	}

	@Override
	public ClassId getOwnerId() {
		return owner;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!o.getClass().equals(getClass())) return false;

		MethodId m = (MethodId) o;
		return key.equals(m.key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public int compareTo(MethodId o) {
		if (owner.equals(o.owner)) {
			return o.key.index - key.index;
		}
		return owner.compareTo(o.owner);
	}
}
