package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;

class MethodRecord implements AttributeRecord {

	final Weaver weaver;
	final short id;
	final ClassRecord parent;
	final String name;
	String signature;
	String[] exceptions;
	int access;
	String description;

	MethodRecord(ClassRecord parent, short id, String name) {
		this.weaver = parent.weaver;
		this.id = id;
		this.parent = parent;
		this.name = name;
	}

	public void init(int access, String description, String signature, String[] exceptions) {
		this.access = access;
		this.description = description;
		this.signature = signature;
		this.exceptions = exceptions;
	}

	public Method toAttribute(Class cls) {
		ClassId cid = cls.getId();
		MethodId mid = new MethodId(cid.datasetValue(), cid.indexValue(), id);
		return new Method(mid, name, cls, description, access);
	}

	public boolean isNative() {
		return (0x800 & access) != 0;
	}

	public boolean isMain() {
		return "main".equals(name) && "([Ljava/lang/String;)V".equals(description)
		&& (0x1 | 0x8) == access; // public, static
	}

	public boolean isInit() {
		return name.equals("<init>");
	}

	public boolean isCLInit() {
		return name.equals("<clinit>");
	}

	public boolean isEquals() {
		return "equals".equals(name) && "(Ljava/lang/Object;)Z".equals(description)
		&& 0x1 == access; // public
	}

	public boolean isHashCode() {
		return "hashCode".equals(name) && "()I".equals(description)
		&& 0x1 == access; // public
	}

	public boolean isStatic() {
		return (0x8 & access) != 0; // static
	}
}
