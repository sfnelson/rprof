/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class Instance implements DataObject<InstanceId, Instance> {

	public static final java.lang.Class<Instance> TYPE = Instance.class;

	@NotNull
	private InstanceId id;

	@Nullable
	private ClazzId type;

	@Nullable
	protected MethodId constructor;

	public Instance() {
	}

	public Instance(@NotNull InstanceId id, @Nullable ClazzId type,
					@Nullable MethodId constructor) {
		this.id = id;
		this.type = type;
		this.constructor = constructor;
	}

	@NotNull
	@Override
	public InstanceId getId() {
		return id;
	}

	@Nullable
	public ClazzId getType() {
		return type;
	}

	public void setType(@Nullable ClazzId type) {
		this.type = type;
	}

	@Nullable
	public MethodId getConstructor() {
		return constructor;
	}

	public void setConstructor(@Nullable MethodId m) {
		this.constructor = m;
	}
}
