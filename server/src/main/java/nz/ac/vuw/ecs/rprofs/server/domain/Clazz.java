/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class Clazz implements DataObject<ClazzId, Clazz> {

	public static final java.lang.Class<Clazz> TYPE = Clazz.class;

	public static final int CLASS_VERSION_UPDATED = 0x1;
	public static final int CLASS_IGNORED_PACKAGE_FILTER = 0x2;
	public static final int SPECIAL_CLASS_WEAVER = 0x4;

	@NotNull
	private ClazzId id;

	@Nullable
	private String name;

	@Nullable
	private ClazzId parent;

	@Nullable
	private String parentName;

	private int properties;

	public Clazz() {
	}

	public Clazz(@NotNull ClazzId id, @Nullable String name,
				 @Nullable ClazzId parent, @Nullable String parentName,
				 int properties) {
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.parentName = parentName;
		this.properties = properties;
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

	public void setParent(@Nullable ClazzId parent) {
		this.parent = parent;
	}

	@Nullable
	public ClazzId getParent() {
		return parent;
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

	public static String getPackageName(String fqname) {
		int last = fqname.lastIndexOf('/');
		if (last < 0) last = 0;
		return fqname.replace('/', '.').substring(0, last);
	}

	public static String getSimpleName(String fqname) {
		return fqname.substring(fqname.lastIndexOf('/') + 1);
	}
}
