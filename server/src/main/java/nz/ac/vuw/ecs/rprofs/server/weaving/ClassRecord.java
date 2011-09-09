package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;

import java.util.Map;
import java.util.Set;

public class ClassRecord {

	final Weaver weaver;
	final Clazz cls;
	final ClassId id;

	String name;
	String superName;
	int version;
	int access;
	int properties;
	String signature;
	String[] interfaces;
	Set<FieldRecord> watches = Collections.newSet();

	Map<Short, MethodRecord> methods = Collections.newMap();
	Map<Short, FieldRecord> fields = Collections.newMap();

	ClassRecord(Weaver weaver, Clazz cls) {
		this.weaver = weaver;
		this.cls = cls;
		this.id = cls.getId();
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

	public Clazz toClass(Dataset owner) {
		// TODO update the provided class
		return new Clazz(owner, id, name, null, properties);
	}

	public FieldRecord getField(String name, String desc) {
		for (FieldRecord fr : fields.values()) {
			if (name.equals(fr.name) && desc.equals(fr.description)) {
				return fr;
			}
		}

		return null;
	}

	public Map<Short, MethodRecord> getMethods() {
		return methods;
	}

	public Map<Short, FieldRecord> getFields() {
		return fields;
	}

	public String getSuperName() {
		return superName;
	}
}
