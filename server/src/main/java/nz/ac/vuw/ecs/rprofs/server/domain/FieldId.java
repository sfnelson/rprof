package nz.ac.vuw.ecs.rprofs.server.domain;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FieldId implements AttributeId, IsSerializable, Comparable<FieldId> {

	Field.PK key;

	ClassId owner;
	String name;

	public FieldId() {}

	public FieldId(ClassId owner, int index, String name) {
		this.key = new Field.PK();
		this.key.owner_index = owner.index;
		this.key.index = index;
		this.owner = owner;
		this.name = name;
	}

	public ClassId getOwnerId() {
		return owner;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!o.getClass().equals(getClass())) return false;

		FieldId f = (FieldId) o;
		return key.equals(f.key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public int compareTo(FieldId o) {
		if (owner.equals(o.owner)) {
			return o.key.index - key.index;
		}
		return owner.compareTo(o.owner);
	}
}
