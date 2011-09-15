package nz.ac.vuw.ecs.rprofs.server.weaving;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassRecord {

	private static final Logger log = LoggerFactory.getLogger(ClassRecord.class);

	private final Clazz clazz;

	private final Set<Field> watches = Sets.newHashSet();

	private final Map<String, Method> methods = Maps.newHashMap();
	private final Map<String, Field> fields = Maps.newHashMap();

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

	public void addMethods(List<? extends Method> methods) {
		for (Method m : methods) {
			this.methods.put(m.getName() + m.getDescription(), m);
		}
	}

	public void addFields(List<? extends Field> fields) {
		for (Field f : fields) {
			this.fields.put(f.getName() + f.getDescription(), f);
			if ((Opcodes.ACC_STATIC & f.getAccess()) == 0) {
				watches.add(f);
			}
		}
	}

	public void generateMethod(String name, String desc, int access) {
		Method m = new Method(new MethodId(clazz.getId().datasetValue(),
				clazz.getId().indexValue(), (short) 0),
				name, clazz.getId(), clazz.getName(), desc, access);
		methods.put(name + desc, m); // TODO do we want to store this?
	}

	public int getProperties() {
		return clazz.getProperties();
	}

	void setProperties(int properties) {
		clazz.setProperties(properties);
	}

	@Nullable
	Method getMethod(@NotNull String name, @NotNull String desc) {
		Method result = methods.get(name + desc);
		if (result == null) {
			log.warn("method not found: {}{}", name, desc);
		}
		return result;
	}

	@Nullable
	Field getField(String name, String desc) {
		Field result = fields.get(name + desc);
		if (result == null) {
			log.warn("field not found: {}{} ({})", new Object[]{name, desc, fields});
		}
		return result;
	}

	Set<Field> getWatches() {
		return watches;
	}
}
