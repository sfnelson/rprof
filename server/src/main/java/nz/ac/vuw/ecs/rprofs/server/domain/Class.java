/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table( name = "classes" )
@NamedQueries({
	@NamedQuery(name="numPackages", query="select count(packageName) from Class group by packagename"),
	@NamedQuery(name="allPackages", query="select packageName from Class group by packagename"),
	@NamedQuery(name="numClassesForPackage", query="select count(C) from Class C where C.packageName = :package"),
	@NamedQuery(name="classesForPackage", query="select C from Class C where C.packageName = :package"),
	@NamedQuery(name="numClasses", query="select count(C) from Class C"),
	@NamedQuery(name="allClasses", query="select C from Class C"),
})
public class Class implements DataObject<Class> {

	public static final java.lang.Class<Class> TYPE = Class.class;

	public static final int CLASS_VERSION_UPDATED = 0x1;
	public static final int CLASS_IGNORED_PACKAGE_FILTER = 0x2;
	public static final int SPECIAL_CLASS_WEAVER = 0x4;

	@EmbeddedId
	private ClassId id;

	private String packageName;
	private String simpleName;
	private String fqname;

	private Integer properties;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Class parent;

	@Version
	private int version;

	public Class() {}

	public Class(ClassId id, String name, Class parent, int properties) {
		this.id = id;
		this.fqname = name;
		this.parent = parent;
		this.properties = properties;

		int last = name.lastIndexOf('/');
		if (last < 0) last = 0;
		packageName = name.replace('/', '.').substring(0, last);

		simpleName = name.substring(name.lastIndexOf('/') + 1);
	}

	public ClassId getId() {
		return id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setParent(Class parent) {
		this.parent = parent;
	}

	public Class getParent() {
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
