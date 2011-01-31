package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Field.FieldId;

import org.objectweb.asm.Opcodes;

class FieldRecord implements AttributeRecord {

	final Weaver weaver;
	final ClassRecord parent;
	final int id;
	final String name;
	String description;
	int access;
	boolean equals;
	boolean hash;

	FieldRecord(ClassRecord parent, int id, String name) {
		this.weaver = parent.weaver;
		this.parent = parent;
		this.id = id;
		this.name = name;
	}

	void init(int access, String description) {
		this.description = description;
		this.access = access;
		if ((Opcodes.ACC_STATIC & access) == 0) {
			parent.watches.add(this);
		}
	}

	void setEquals(boolean equals) {
		this.equals = equals;
	}

	void setHash(boolean hash) {
		this.hash = hash;
	}

	public Field toAttribute(Class cls) {
		return new Field(new FieldId(cls.getClassId().getIndex(), id), name, cls, description, access, equals, hash);
	}
}
