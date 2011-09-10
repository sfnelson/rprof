/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.model.Attribute;

import javax.persistence.*;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
@Entity
@NamedQueries({
		@NamedQuery(name = "fieldsForType", query = "select F from Field F where F.owner = :type"),
		@NamedQuery(name = "deleteFields", query = "delete from Field F")
})
public class Field implements Attribute<Field> {

	public static final java.lang.Class<Field> TYPE = Field.class;

	@EmbeddedId
	FieldId id;

	@Version
	Integer version;

	@ManyToOne
	private Clazz owner;

	private String name;

	protected String description;

	protected int access;

	protected boolean equals;

	protected boolean hash;

	public Field() {
	}

	public Field(FieldId id, String name, Clazz owner, String desc, int access) {
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
		return id.longValue();
	}

	public Integer getVersion() {
		return version;
	}

	@Override
	public Clazz getOwner() {
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

	public void visit(DomainVisitor visitor) {
		// TODO visitField()
	}
}
