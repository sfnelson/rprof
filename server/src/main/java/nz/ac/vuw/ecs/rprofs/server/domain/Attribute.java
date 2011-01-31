package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.MappedSuperclass;

import nz.ac.vuw.ecs.rprofs.server.domain.Class.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.IntIntId;


public interface Attribute {

	@SuppressWarnings("serial")
	@MappedSuperclass
	public class AttributeId extends IntIntId {
		public AttributeId() {}
		public AttributeId(int owner_index, int index) {
			super(owner_index, index);
		}
	}

	long getId();
	AttributeId getAttributeId();
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
