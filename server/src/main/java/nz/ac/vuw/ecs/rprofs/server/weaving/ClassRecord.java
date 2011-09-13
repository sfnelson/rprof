package nz.ac.vuw.ecs.rprofs.server.weaving;

import com.google.common.collect.Maps;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassRecord {

	private final Clazz clazz;

	private String name;
	private int properties;
	private Set<Field> watches = Collections.newSet();

	private Map<Short, Method> methods = Maps.newHashMap();
	private Map<Short, Field> fields = Maps.newHashMap();

	public ClassRecord(Clazz clazz) {
		this.clazz = clazz;
	}

	public Clazz getClazz() {
		return clazz;
	}

	public ClazzId getId() {
		return clazz.getId();
	}

	public String getName() {
		return clazz.getName();
	}

	public void addMethods(List<Method> methods) {
		for (Method m : methods) {
			this.methods.put(m.getId().attributeValue(), m);
		}
	}

	public void addFields(List<Field> fields) {
		for (Field f : fields) {
			this.fields.put(f.getId().attributeValue(), f);
			if ((Opcodes.ACC_STATIC & f.getAccess()) == 0) {
				watches.add(f);
			}
		}
	}

	public int getProperties() {
		return properties;
	}

	void setProperties(int properties) {
		this.properties = properties;
	}

	Map<Short, Method> getMethods() {
		return methods;
	}

	Method getMethod(String name, String desc) {
		for (Method m : methods.values()) {
			if (m.getName().equals(name) && m.getDescription().equals(desc)) {
				return m;
			}
		}

		return null;
	}

	Map<Short, Field> getFields() {
		return fields;
	}

	Field getField(String name, String desc) {
		for (Field f : fields.values()) {
			if (name.equals(f.getName()) && desc.equals(f.getDescription())) {
				return f;
			}
		}

		return null;
	}

	Set<Field> getWatches() {
		return watches;
	}
}
