/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import java.util.ArrayList;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClassData extends ClassInfo implements IsSerializable {

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

	@Override
	public List<MethodData> getMethods() {
		return Collections.immutable(methods);
	}

	@Override
	public List<FieldData> getFields() {
		return Collections.immutable(fields);
	}

	@Override
	public int getFlags() {
		return flags;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ClassInfo getParent() {
		return parent;
	}
}