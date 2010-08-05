package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;

public class MethodRecord implements Serializable {
	private static final long serialVersionUID = -8480719569305048438L;

	public int id;
	public String name;
	public String desc;
	
	public MethodRecord() {}
	protected MethodRecord(int id, String name, String desc) {
		this.id = id;
		this.name = name;
		this.desc = desc;
	}

	public MethodRecord toRPC() {
		return new MethodRecord(id, name, desc);
	}
}
