package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClassRecord<T extends MethodRecord> implements Serializable {
	
	private static final long serialVersionUID = 2390564187873117774L;

	public int id;
	public String name;
	public int instances;
	private List<T> methods = new ArrayList<T>();
	
	public ClassRecord() {}
	protected ClassRecord(int id, String name, int instances) {
		this.id = id;
		this.name = name;
		this.instances = instances;
	}
	
	public List<T> getMethods() {
		return methods;
	}
	
	public ClassRecord<MethodRecord> toRPC() {
		ClassRecord<MethodRecord> cr = new ClassRecord<MethodRecord>(id, name, instances);
		
		for (T mr: getMethods()) {
			cr.methods.add(mr.toRPC());
		}
		
		return cr;
	}

	public String getPackage() {
		int last = name.lastIndexOf('/');
		if (last < 0) last = 0;
		return name.replace('/', '.').substring(0, last);
	}
	
	public String getClassName() {
		int last = name.lastIndexOf('/');
		return name.substring(last + 1);
	}
}
