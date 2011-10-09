/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.model.Attribute;

import javax.validation.constraints.NotNull;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class Field implements Attribute<FieldId, Field> {

	@NotNull
	private FieldId id;

	@NotNull
	private Integer version;

	@NotNull
	private String name;

	@NotNull
	private ClazzId owner;

	@NotNull
	private String ownerName;

	@NotNull
	protected String description;

	protected int access;

	public Field() {
	}

	public Field(@NotNull FieldId id, @NotNull Integer version, @NotNull String name,
				 @NotNull ClazzId owner, @NotNull String ownerName, @NotNull String desc,
				 int access) {
		this.id = id;
		this.version = version;
		this.name = name;
		this.owner = owner;
		this.ownerName = ownerName;
		this.description = desc;
		this.access = access;
	}

	@Override
	@NotNull
	public FieldId getId() {
		return id;
	}

	@NotNull
	public Integer getVersion() {
		return version;
	}

	public void setVersion(@NotNull Integer version) {
		this.version = version;
	}

	@Override
	@NotNull
	public ClazzId getOwner() {
		return owner;
	}

	@Override
	@NotNull
	public String getOwnerName() {
		return ownerName;
	}

	@Override
	@NotNull
	public String getName() {
		return name;
	}

	@Override
	@NotNull
	public String getDescription() {
		return description;
	}

	public int getAccess() {
		return access;
	}

	@Override
	@NotNull
	public String toString() {
		return ownerName + "." + name + ":" + description;
	}

	@Override
	public void visit(@NotNull AttributeVisitor visitor) {
		visitor.visitField(this);
	}
}
