package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;

public class MethodRecord implements Serializable {
	private static final long serialVersionUID = -8480719569305048438L;

	public int id;
	public String name;
	
	public MethodRecord() {}
	protected MethodRecord(int id, String name) {
		this.id = id;
		this.name = name;
	}
	/**
	 * @return
	 */
	public MethodRecord toRPC() {
		return new MethodRecord(id, name);
	}
}
