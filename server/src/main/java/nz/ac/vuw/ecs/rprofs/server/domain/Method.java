/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table(name = "methods")
@NamedQueries({
	@NamedQuery(name = "methodsForType", query = "select M from Method M where M.owner = :type")
})
public class Method implements Attribute<Method> {

	public static final java.lang.Class<Method> TYPE = Method.class;

	@EmbeddedId
	private MethodId mid;

	@Version
	private Integer version;

	private String name;

	@ManyToOne
	private Class owner;

	private String description;

	private Integer access;

	public Method() {}

	public Method(MethodId id, String name, Class owner, String description, int access) {
		this.mid = id;
		this.name = name;
		this.owner = owner;
		this.description = description;
		this.access = access;
	}

	public MethodId getId() {
		return mid;
	}

	public Integer getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public Class getOwner() {
		return owner;
	}

	public ClassId getOwnerId() {
		return owner.getId();
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