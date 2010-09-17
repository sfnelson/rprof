package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;

public class FieldRecord implements Serializable {
	private static final long serialVersionUID = -2753149845892424476L;
	
	public int id;
	public String name;
	public String desc;
	public boolean equals;
	public boolean hash;
	
	public FieldRecord() {}
	protected FieldRecord(int id, String name, String desc, boolean equals, boolean hash) {
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.equals = equals;
		this.hash = hash;
	}

	public FieldRecord toRPC() {
		return new FieldRecord(id, name, desc, equals, hash);
	}
}
