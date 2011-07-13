/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.model.Attribute;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table(name = "fields")
@NamedQueries({
	@NamedQuery(name = "fieldsForType", query = "select F from Field F where F.owner = :type"),
	@NamedQuery(name = "deleteFields", query = "delete Field F where F.owner.owner = :dataset")
})
public class Field implements Attribute<Field> {

	public static final java.lang.Class<Field> TYPE = Field.class;

	@EmbeddedId
	FieldId id;

	@Version
	Integer version;

	@ManyToOne
	@JoinColumn(name="owner_id", nullable=false)
	private Class owner;

	private String name;

	protected String description;

	protected int access;

	protected boolean equals;

	protected boolean hash;

	Field() {}

	public Field(FieldId id, String name, Class owner, String desc, int access) {
		this.id = id;
		this.owner = owner;
		this.name = name;
		this.description = desc;
		this.access = access;
	}

	public FieldId getId() {
		return id;
	}

	public Long getRpcId() {
		return id.getId();
	}

	public Integer getVersion() {
		return version;
	}

	@Override
	public Class getOwner() {
		return owner;
	}

	@Override
	public ClassId getOwnerId() {
		return owner.getId();
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

	@Override
	public String toString() {
		return owner.getName() + "." + name + ":" + description;
	}

	@Override
	public void visit(AttributeVisitor visitor) {
		visitor.visitField(this);
	}
}
