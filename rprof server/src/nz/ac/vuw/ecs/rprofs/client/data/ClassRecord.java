package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;
import java.util.ArrayList;

public class ClassRecord implements Serializable {
	
	private static final long serialVersionUID = 2390564187873117774L;

	public int id;
	
	public String request;
	public int requestLength;
	
	public String[] headers;
	public String[] values;
	
	public int responseLength;

	public int version;
	public int access;
	public String name;
	public String signature;
	public String superName;
	public String[] interfaces;

	public ArrayList<MethodRecord> methods = new ArrayList<MethodRecord>();
	
	public int id() {
		return id;
	}
	
	public void init(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.version = version;
		this.access = access;
		this.name = name;
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
	}

}
