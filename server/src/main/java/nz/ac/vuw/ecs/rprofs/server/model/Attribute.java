package nz.ac.vuw.ecs.rprofs.server.model;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.AttributeId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;

import javax.validation.constraints.NotNull;


public interface Attribute<I extends AttributeId<I, T>, T extends Attribute<I, T>>
		extends DataObject<I, T> {

	@NotNull
	I getId();

	@NotNull
	ClazzId getOwner();

	@NotNull
	String getOwnerName();

	@NotNull
	String getName();

	@NotNull
	String getDescription();

	void visit(@NotNull AttributeVisitor visitor);

	public interface AttributeVisitor {
		void visitField(@NotNull Field field);

		void visitMethod(@NotNull Method method);
	}
}
