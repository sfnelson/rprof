package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FieldData extends FieldInfo implements IsSerializable {
	public int id;
	public String name;
	public String desc;
	public int access;
	public boolean equals;
	public boolean hash;
	
	public FieldData() {}
	protected FieldData(int id, String name, String desc, int access, boolean equals, boolean hash) {
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.equals = equals;
		this.hash = hash;
	}

	public int getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return desc; }
	public int getAccess() { return access; }
	public boolean inEquals() { return equals; }
	public boolean inHashCode() { return hash; }
}
