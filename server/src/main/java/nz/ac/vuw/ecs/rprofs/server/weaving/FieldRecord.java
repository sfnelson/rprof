package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import org.objectweb.asm.Opcodes;

class FieldRecord implements AttributeRecord {

	final Weaver weaver;
	final ClassRecord parent;
	final short id;
	final String name;
	String description;
	int access;

	FieldRecord(ClassRecord parent, short id, String name) {
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

	public Field toAttribute(Clazz cls) {
		ClassId cid = cls.getId();
		FieldId fid = new FieldId(cid.datasetValue(), cid.indexValue(), id);
		return new Field(fid, name, cls, description, access);
	}
}
