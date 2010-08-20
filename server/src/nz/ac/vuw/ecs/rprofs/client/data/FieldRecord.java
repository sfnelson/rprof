package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;

public class FieldRecord implements Serializable {
	private static final long serialVersionUID = -2753149845892424476L;
	
	public int id;
	public String name;
	public String desc;
	
	public FieldRecord() {}
	protected FieldRecord(int id, String name, String desc) {
		this.id = id;
		this.name = name;
		this.desc = desc;
	}

	public FieldRecord toRPC() {
		return new FieldRecord(id, name, desc);
	}
}
