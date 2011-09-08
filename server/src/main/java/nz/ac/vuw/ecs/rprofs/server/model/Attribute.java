package nz.ac.vuw.ecs.rprofs.server.model;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.AttributeId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;


public interface Attribute<T extends Attribute<T>> extends DataObject<T, AttributeId<T>> {

	AttributeId<T> getId();
	ClassId getOwnerId();

	Clazz getOwner();
	String getName();
	String getDescription();

	void visit(AttributeVisitor visitor);

	public interface AttributeVisitor {
		void visitField(Field field);
		void visitMethod(Method method);
	}
}
