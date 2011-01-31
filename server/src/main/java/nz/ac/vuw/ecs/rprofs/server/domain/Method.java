/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.server.domain.Class.ClassId;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table(name = "methods")
public class Method implements Attribute {

	@SuppressWarnings("serial")
	@Embeddable
	public static class MethodId extends AttributeId {
		public MethodId() {}
		public MethodId(int fst, int snd) {
			super(fst, snd);
		}
	}

	@EmbeddedId
	protected MethodId id;

	@Version
	private int version;

	String name;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "owner_id", nullable = false)
	private Class owner;

	protected String description;

	protected Integer access;

	public Method() {}

	public Method(MethodId id, String name, Class owner, String description, int access) {
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.description = description;
		this.access = access;
	}

	public long getId() {
		return id.getId();
	}

	public MethodId getAttributeId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public int getIndex() {
		return id.getIndex();
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
