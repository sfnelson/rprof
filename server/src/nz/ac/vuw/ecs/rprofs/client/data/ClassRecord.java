package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClassRecord<M extends MethodRecord, F extends FieldRecord> implements Serializable, Comparable<ClassRecord<?, ?>> {
	
	private static final long serialVersionUID = 2390564187873117774L;
	
	public static final int CLASS_VERSION_UPDATED = 0x1;
	public static final int CLASS_IGNORED_PACKAGE_FILTER = 0x2;
	public static final int SPECIAL_CLASS_WEAVER = 0x4;

	public int id;
	public String name;
	public int flags;
	private List<M> methods = new ArrayList<M>();
	private List<F> fields = new ArrayList<F>();
	
	public ClassRecord() {}
	protected ClassRecord(int id, String name, int flags) {
		this.id = id;
		this.name = name;
		this.flags = flags;
	}
	
	public List<M> getMethods() {
		return methods;
	}
	
	public List<F> getFields() {
		return fields;
	}
	
	public ClassRecord<MethodRecord, FieldRecord> toRPC() {
		ClassRecord<MethodRecord, FieldRecord> cr
			= new ClassRecord<MethodRecord, FieldRecord>(id, name, flags);
		
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

	@Override
	public int compareTo(ClassRecord<?, ?> o) {
		return name.compareTo(o.name);
	}
}
