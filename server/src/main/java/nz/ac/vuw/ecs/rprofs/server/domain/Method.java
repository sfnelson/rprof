/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.model.Attribute;

import javax.validation.constraints.NotNull;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class Method implements Attribute<MethodId, Method> {

	@NotNull
	private MethodId mid;

	@NotNull
	private String name;

	@NotNull
	private ClazzId owner;

	@NotNull
	private String ownerName;

	@NotNull
	private String description;

	private int access;

	public Method() {
	}

	public Method(MethodId id, String name, ClazzId owner, String ownerName,
				  String description, int access) {
		this.mid = id;
		this.name = name;
		this.owner = owner;
		this.ownerName = ownerName;
		this.description = description;
		this.access = access;
	}

	@Override
	@NotNull
	public MethodId getId() {
		return mid;
	}

	@NotNull
	public String getName() {
		return name;
	}

	@NotNull
	public ClazzId getOwner() {
		return owner;
	}

	@NotNull
	public String getOwnerName() {
		return ownerName;
	}

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
		visitor.visitMethod(this);
	}
}
