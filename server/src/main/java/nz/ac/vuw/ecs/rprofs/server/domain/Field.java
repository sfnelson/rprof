/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.io.Serializable;

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
@Table(name = "fields")
public class Field implements Attribute {

	@SuppressWarnings("serial")
	@Embeddable
	public static class FieldId extends AttributeId implements Serializable {
		public FieldId() {}
		public FieldId(int owner_index, int index) {
			super(owner_index, index);
		}
	}

	@EmbeddedId
	FieldId id;

	@Version
	int version;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_id", nullable = false)
	private Class owner;

	private String name;

	protected String description;

	protected int access;

	protected boolean equals;

	protected boolean hash;

	Field() {}

	public Field(FieldId id, String name, Class owner, String desc, int access, boolean equals, boolean hash) {
		this.id = id;
		this.owner = owner;
		this.name = name;
		this.description = desc;
		this.equals = equals;
		this.hash = hash;
	}

	public long getId() {
		return id.getId();
	}

	public FieldId getAttributeId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public int getIndex() {
		return id.getIndex();
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

	public boolean getEquals() {
		return equals;
	}

	public boolean getHash() {
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
		return new Field(f.id, f.name, owner, f.description, f.access, f.equals, f.hash);
	}

	public void setEquals(boolean b) {
		this.equals = b;
	}

	public void setHash(boolean b) {
		this.hash = b;
	}

}
