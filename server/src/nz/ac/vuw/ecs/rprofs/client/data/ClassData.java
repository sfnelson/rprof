/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import java.util.ArrayList;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClassData extends ClassInfo<ClassData, MethodData, FieldData> implements IsSerializable {

	public int id;
	public String name;
	public int flags;
	public ClassData parent;

	ArrayList<MethodData> methods;
	ArrayList<FieldData> fields;

	public ClassData() {}
	protected ClassData(int id, String name, int flags, ClassData parent) {
		this.id = id;
		this.name = name;
		this.flags = flags;
		this.parent = parent;
	}

	public List<MethodData> getMethods() { return Collections.immutable(methods); }
	public List<FieldData> getFields() { return Collections.immutable(fields); }
	public int getFlags() { return flags; }
	public int getId() { return id; }
	public String getName() { return name; }
	public ClassData getSuper() { return parent; }
}