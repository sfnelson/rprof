/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

import javax.persistence.*;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
@Entity
@NamedQueries({
		@NamedQuery(name = "numPackages", query = "select count(C.packageName) from Clazz C group by C.packageName"),
		@NamedQuery(name = "allPackages", query = "select C.packageName from Clazz C group by C.packageName"),
		@NamedQuery(name = "numClassesForPackage", query = "select count(C) from Clazz C where C.packageName = :package"),
		@NamedQuery(name = "classesForPackage", query = "select C from Clazz C where C.packageName = :package"),
		@NamedQuery(name = "numClasses", query = "select count(C) from Clazz C"),
		@NamedQuery(name = "allClasses", query = "select C from Clazz C"),
		@NamedQuery(name = "findClassByName", query = "select C from Clazz C where C.fqname = :name"),
		@NamedQuery(name = "deleteClasses", query = "delete Clazz C")
})
public class Clazz implements DataObject<Clazz, ClassId> {

	public static final java.lang.Class<Clazz> TYPE = Clazz.class;

	public static final int CLASS_VERSION_UPDATED = 0x1;
	public static final int CLASS_IGNORED_PACKAGE_FILTER = 0x2;
	public static final int SPECIAL_CLASS_WEAVER = 0x4;

	@EmbeddedId
	private ClassId id;

	@Transient
	private Dataset owner;

	private String packageName;
	private String simpleName;
	private String fqname;

	private Integer properties;

	@ManyToOne
	private Clazz parent;

	@Version
	private int version;

	public Clazz() {
	}

	public Clazz(Dataset owner, ClassId id, String name, Clazz parent, int properties) {
		this.owner = owner;
		this.id = id;
		this.fqname = name;
		this.parent = parent;
		this.properties = properties;

		int last = name.lastIndexOf('/');
		if (last < 0) last = 0;
		packageName = name.replace('/', '.').substring(0, last);

		simpleName = name.substring(name.lastIndexOf('/') + 1);
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

	public ClassId getId() {
		return id;
	}

	public Long getRpcId() {
		return id.longValue();
	}

	public Integer getVersion() {
		return version;
	}

	public Dataset getOwner() {
		return owner;
	}

	public void setParent(Clazz parent) {
		this.parent = parent;
	}

	public Clazz getParent() {
		return parent;
	}

	public ClassId getParentId() {
		return parent.getId();
	}

	public String getName() {
		return fqname;
	}

	public int getProperties() {
		return properties;
	}

	public String getPackage() {
		return packageName;
	}

	public String getSimpleName() {
		return simpleName;
	}
}
