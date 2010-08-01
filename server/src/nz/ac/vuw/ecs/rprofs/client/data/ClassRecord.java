package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClassRecord<T extends MethodRecord> implements Serializable {
	
	private static final long serialVersionUID = 2390564187873117774L;

	public int id;
	public String name;
	private List<T> methods = new ArrayList<T>();
	
	public ClassRecord() {}
	protected ClassRecord(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public List<T> getMethods() {
		return methods;
	}
	
	public ClassRecord<MethodRecord> toRPC() {
		ClassRecord<MethodRecord> cr = new ClassRecord<MethodRecord>(id, name);
		
		for (T mr: getMethods()) {
			cr.methods.add(mr.toRPC());
		}
		
		return cr;
	}
}
