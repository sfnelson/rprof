/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "fields")
public class Field implements Serializable, Attribute {

	@Embeddable
	static class PK implements Serializable {
		int owner_index;
		int index;

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o == null) return false;
			if (o.getClass() != this.getClass()) return false;
			PK k = (PK) o;
			return owner_index == k.owner_index && index == k.index;
		}

		@Override
		public int hashCode() {
			return owner_index ^ index;
		}
	}

	@Transient
	private FieldId id;

	@EmbeddedId
	PK key;

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private Class owner;

	private String name;

	protected String description;

	protected int access;

	protected boolean equals;

	protected boolean hash;

	Field() {}

	public Field(FieldId id, Class owner, String desc, int access, boolean equals, boolean hash) {
		this.id = id;
		this.key = id.key;
		this.owner = owner;
		this.name = id.name;
		this.description = desc;
		this.equals = equals;
		this.hash = hash;
	}

	public FieldId getId() {
		if (id == null) {
			id = new FieldId(owner.getClassId(), key.index, name);
		}
		return id;
	}

	public int getIndex() {
		return key.index;
	}

	@Override
	public Class getOwner() {
		return owner;
	}

	@Override
	public ClassId getOwnerId() {
		return owner.getClassId();
	}

	@Override
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getAccess() {
		return access;
	}

	public boolean isEquals() {
		return equals;
	}

	public boolean isHash() {
		return hash;
	}

	@Override
	public String toString() {
		return owner.getName() + "." + name + ":" + description;
	}

	@Override
	public void visit(AttributeVisitor visitor) {
		visitor.visitField(this);
	}

	public static Field clone(Field f, Class owner) {
		return new Field(f.id, owner, f.description, f.access, f.equals, f.hash);
	}

	public void setEquals(boolean b) {
		this.equals = b;
	}

	public void setHash(boolean b) {
		this.hash = b;
	}

}
