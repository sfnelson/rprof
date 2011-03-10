package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.AttributeId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;


public interface Attribute<T extends Attribute<T>> extends DataObject<T> {

	AttributeId<T> getId();
	ClassId getOwnerId();

	Class getOwner();
	String getName();
	String getDescription();

	void visit(AttributeVisitor visitor);

	public interface AttributeVisitor {
		void visitField(Field field);
		void visitMethod(Method method);
	}
}
