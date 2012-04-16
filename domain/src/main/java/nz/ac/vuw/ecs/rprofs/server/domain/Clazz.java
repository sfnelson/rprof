/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class Clazz implements DataObject<ClazzId, Clazz> {

	public static final int CLASS_VERSION_UPDATED = 0x1;
	public static final int SPECIAL_CLASS_WEAVER = 0x2;
	public static final int CLASS_INCLUDE_MATCHED = 0x4;
	public static final int CLASS_EXCLUDE_MATCHED = 0x8;
	public static final int COLLECTION = 0x10;

	@NotNull
	private ClazzId id;

	@NotNull
	private Integer version;

	@Nullable
	private String name;

	@Nullable
	private ClazzId parent;

	@Nullable
	private String parentName;

	private int properties;

	private int access;

	private boolean initialized;

	public Clazz() {
	}

	public Clazz(@NotNull ClazzId id, @NotNull Integer version, @Nullable String name,
				 @Nullable ClazzId parent, @Nullable String parentName,
				 int properties, int access, boolean initialized) {
		this.id = id;
		this.version = version;
		this.name = name;
		this.parent = parent;
		this.parentName = parentName;
		this.properties = properties;
		this.access = access;
		this.initialized = initialized;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj.getClass() != getClass()) return false;
		Clazz cls = (Clazz) obj;
		return id.equals(cls.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	@NotNull
	public ClazzId getId() {
		return id;
	}

	@NotNull
	public Integer getVersion() {
		return version;
	}

	public void setVersion(@NotNull Integer version) {
		this.version = version;
	}

	public void setParent(@Nullable ClazzId parent) {
		this.parent = parent;
	}

	@Nullable
	public ClazzId getParent() {
		return parent;
	}

	public void setParentName(@Nullable String parentName) {
		this.parentName = parentName;
	}

	@Nullable
	public String getParentName() {
		return parentName;
	}

	@Nullable
	public String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	public int getProperties() {
		return properties;
	}

	public void setProperties(int properties) {
		this.properties = properties;
	}

	public int getAccess() {
		return access;
	}

	public void setAccess(int access) {
		this.access = access;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public static String getPackageName(String fqname) {
		int last = fqname.lastIndexOf('/');
		if (last < 0) last = 0;
		return fqname.replace('/', '.').substring(0, last);
	}

	public static String getSimpleName(String fqname) {
		return fqname.substring(fqname.lastIndexOf('/') + 1);
	}
}
