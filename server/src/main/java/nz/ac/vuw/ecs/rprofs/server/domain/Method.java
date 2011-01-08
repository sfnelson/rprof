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
@Table(name = "methods")
public class Method implements Attribute, Serializable {

	@Transient
	protected MethodId id;

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

		@Override
		public String toString() {
			return String.format("%d.%d", owner_index, index);
		}
	}

	@EmbeddedId
	PK key;

	String name;

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private Class owner;

	protected String description;

	protected Integer access;

	public Method() {}

	public Method(MethodId id, Class owner, String description, int access) {
		this.id = id;
		this.key = id.key;
		this.name = id.name;
		this.owner = owner;
		this.description = description;
		this.access = access;
	}

	public MethodId getId() {
		if (id == null) {
			id = new MethodId(owner.getClassId(), key.index, name);
		}
		return id;
	}

	public int getIndex() {
		return key.index;
	}

	public String getName() {
		return name;
	}

	public Class getOwner() {
		return owner;
	}

	public ClassId getOwnerId() {
		return owner.getClassId();
	}

	public String getDescription() {
		return description;
	}

	public int getAccess() {
		return access;
	}

	@Override
	public String toString() {
		return owner.getName() + "." + name + ":" + description;
	}

	public boolean isNative() {
		return (0x800 & getAccess()) != 0;
	}

	public boolean isMain() {
		return "main".equals(getName()) && "([Ljava/lang/String;)V".equals(getDescription())
		&& (0x1 | 0x8) == getAccess(); // public, static
	}

	public boolean isInit() {
		return getName().equals("<init>");
	}

	public boolean isCLInit() {
		return getName().equals("<clinit>");
	}

	public boolean isEquals() {
		return "equals".equals(getName()) && "(Ljava/lang/Object;)Z".equals(getDescription())
		&& 0x1 == getAccess(); // public
	}

	public boolean isHashCode() {
		return "hashCode".equals(getName()) && "()I".equals(getDescription())
		&& 0x1 == getAccess(); // public
	}

	public boolean isStatic() {
		return (0x8 & getAccess()) != 0; // static
	}

	public String toMethodString() {
		return getName() + ":" + getDescription();
	}

	@Override
	public void visit(AttributeVisitor visitor) {
		visitor.visitMethod(this);
	}
}
