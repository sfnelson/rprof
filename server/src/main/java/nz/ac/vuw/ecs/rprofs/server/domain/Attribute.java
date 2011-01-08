package nz.ac.vuw.ecs.rprofs.server.domain;

public interface Attribute {

	AttributeId getId();
	Class getOwner();
	ClassId getOwnerId();
	String getName();
	String getDescription();

	void visit(AttributeVisitor visitor);

	public interface AttributeVisitor {
		void visitField(Field field);
		void visitMethod(Method method);
	}
}
