package nz.ac.vuw.ecs.rprofs.client.data;

public class MethodRecord {

	public int id;
	
	public ClassRecord parent;
	public int access;
	public String name;
	public String desc;
	public String signature;
	public String[] exceptions;
	
	public MethodRecord() {}
	
	public MethodRecord(ClassRecord parent) {
		this.parent = parent;
	}
	
	public int id() {
		return id;
	}

	public static MethodRecord create(ClassRecord parent, int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodRecord mr = new MethodRecord(parent);
		mr.id = parent.methods.size();
		parent.methods.add(mr.id, mr);
		
		mr.access = access;
		mr.name = name;
		mr.desc = desc;
		mr.signature = signature;
		mr.exceptions = exceptions;
		return mr;
	}
}
