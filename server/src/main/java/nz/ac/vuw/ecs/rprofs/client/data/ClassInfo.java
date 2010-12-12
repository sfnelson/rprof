package nz.ac.vuw.ecs.rprofs.client.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;

public abstract class ClassInfo implements Comparable<ClassInfo> {

	public static final int CLASS_VERSION_UPDATED = 0x1;
	public static final int CLASS_IGNORED_PACKAGE_FILTER = 0x2;
	public static final int SPECIAL_CLASS_WEAVER = 0x4;

	public abstract int getId();
	public abstract String getName();
	public abstract int getFlags();
	public abstract ClassInfo getParent();
	public abstract List<? extends MethodInfo> getMethods();
	public abstract List<? extends FieldInfo> getFields();

	public ClassData toRPC() {
		ClassData s = getParent() == null ? null : getParent().toRPC();
		ClassData cr = new ClassData(getId(), getName(), getFlags(), s);

		cr.methods = Collections.newList();
		for (MethodInfo mr: getMethods()) {
			cr.methods.add(mr.toRPC());
		}

		cr.fields = Collections.newList();
		for (FieldInfo fr: getFields()) {
			cr.fields.add(fr.toRPC());
		}

		return cr;
	}

	public String getPackage() {
		int last = getName().lastIndexOf('/');
		if (last < 0) last = 0;
		return getName().replace('/', '.').substring(0, last);
	}

	public String getClassName() {
		int last = getName().lastIndexOf('/');
		return getName().substring(last + 1);
	}

	public int compareTo(ClassInfo o) {
		return getName().compareTo(o.getName());
	}
}
