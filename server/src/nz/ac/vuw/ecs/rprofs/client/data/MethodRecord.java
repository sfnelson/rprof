package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;

public class MethodRecord implements Serializable {
	private static final long serialVersionUID = -8480719569305048438L;

	public int id;
	public String name;
	public String desc;
	public int access;
	
	public MethodRecord() {}
	protected MethodRecord(int id, String name, String desc, int access) {
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.access = access;
	}

	public MethodRecord toRPC() {
		return new MethodRecord(id, name, desc, access);
	}	

	public boolean isNative() {
		return (0x800 & access) != 0;
	}
	
	public boolean isMain() {
		return "main".equals(name) && "([Ljava/lang/String;)V".equals(desc)
		&& (0x1 | 0x8) == access; // public, static
	}

	public boolean isInit() {
		return name.equals("<init>");
	}
	
	public boolean isCLInit() {
		return name.equals("<clinit>");
	}
	
	public boolean isEquals() {
		return "equals".equals(name) && "(Ljava/lang/Object;)Z".equals(desc)
			&& 0x1 == access; // public
	}
	
	public boolean isHashCode() {
		return "hashCode".equals(name) && "()I".equals(desc)
			&& 0x1 == access; // public
	}
	
	public boolean isStatic() {
		return (0x8 & access) != 0; // static
	}
}
