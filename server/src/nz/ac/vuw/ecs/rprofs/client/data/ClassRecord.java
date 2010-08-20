package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClassRecord<M extends MethodRecord, F extends FieldRecord> implements Serializable {
	
	private static final long serialVersionUID = 2390564187873117774L;

	public int id;
	public String name;
	public int instances;
	private List<M> methods = new ArrayList<M>();
	private List<F> fields = new ArrayList<F>();
	
	public ClassRecord() {}
	protected ClassRecord(int id, String name, int instances) {
		this.id = id;
		this.name = name;
		this.instances = instances;
	}
	
	public List<M> getMethods() {
		return methods;
	}
	
	public List<F> getFields() {
		return fields;
	}
	
	public ClassRecord<MethodRecord, FieldRecord> toRPC() {
		ClassRecord<MethodRecord, FieldRecord> cr
			= new ClassRecord<MethodRecord, FieldRecord>(id, name, instances);
		
		for (M mr: getMethods()) {
			cr.methods.add(mr.toRPC());
		}
		
		for (F fr: getFields()) {
			cr.fields.add(fr.toRPC());
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
