/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.Context;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class MethodRecord extends nz.ac.vuw.ecs.rprofs.client.data.MethodRecord {
	private static final long serialVersionUID = -357201240938009655L;

	public ClassRecord parent;
	
	public int access;
	public String desc;
	public String signature;
	public String[] exceptions;
	
	public MethodRecord() {}
	
	public MethodRecord(ClassRecord parent) {
		this.parent = parent;
	}

	public static MethodRecord create(ClassRecord parent, int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodRecord mr = Context.getInstance().createMethodRecord(parent);
		mr.access = access;
		mr.name = name;
		mr.desc = desc;
		mr.signature = signature;
		mr.exceptions = exceptions;
		return mr;
	}
	
	public String toString() {
		return "m:" + parent + "." + name + ":" + desc;
	}
}
