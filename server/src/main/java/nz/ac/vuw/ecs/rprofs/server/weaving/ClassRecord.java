package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.Map;
import java.util.Set;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Class.ClassId;

class ClassRecord {

	final Weaver weaver;
	final int id;

	String name;
	String superName;
	int version;
	int access;
	int properties;
	String signature;
	String[] interfaces;
	Set<FieldRecord> watches = Collections.newSet();

	Map<Integer, MethodRecord> methods = Collections.newMap();
	Map<Integer, FieldRecord> fields = Collections.newMap();

	ClassRecord(Weaver weaver, int id) {
		this.weaver = weaver;
		this.id = id;
	}

	void init(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.name = name;
		this.version = version;
		this.access = access;
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
	}

	void addMethod(MethodRecord method) {
		methods.put(method.id, method);
	}

	void addField(FieldRecord field) {
		fields.put(field.id, field);
	}

	void setProperties(int properties) {
		this.properties = properties;
	}

	Class toClass() {
		Class cls = new Class(new ClassId(id), name, null, properties);
		for (MethodRecord m: methods.values()) {
			cls.addAttribute(m.toAttribute(cls));
		}
		for (FieldRecord f: fields.values()) {
			cls.addAttribute(f.toAttribute(cls));
		}
		return cls;
	}

	FieldRecord getField(String name, String desc) {
		for (FieldRecord fr: fields.values()) {
			if (name.equals(fr.name) && desc.equals(fr.description)) {
				return fr;
			}
		}

		return null;
	}
}
