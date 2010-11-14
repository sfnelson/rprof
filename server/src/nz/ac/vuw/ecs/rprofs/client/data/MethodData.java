package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;


public class MethodData extends MethodInfo implements IsSerializable {
	
	public int id;
	public String name;
	public String desc;
	public int access;
	
	public MethodData() {}
	protected MethodData(int id, String name, String desc, int access) {
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.access = access;
	}

	public int getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return desc; }
	public int getAccess() { return access; }
}
