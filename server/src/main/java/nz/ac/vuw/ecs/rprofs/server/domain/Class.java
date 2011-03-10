/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Attribute.AttributeVisitor;
import nz.ac.vuw.ecs.rprofs.server.domain.id.AttributeId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table( name = "classes" )
public class Class implements DataObject<Class> {

	public static final java.lang.Class<Class> TYPE = Class.class;

	public static final int CLASS_VERSION_UPDATED = 0x1;
	public static final int CLASS_IGNORED_PACKAGE_FILTER = 0x2;
	public static final int SPECIAL_CLASS_WEAVER = 0x4;

	@EmbeddedId
	private ClassId id;

	private String name;

	private Integer properties;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "parent_id")
	private Class parent;

	@OneToMany(mappedBy="owner", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<Method> methods;

	@OneToMany(mappedBy="owner", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<Field> fields;

	@Transient
	private AttributeVisitor attributeStorer;

	@Version
	private int version;

	public Class() {}

	public Class(ClassId id, String name, Class parent, int properties) {
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.properties = properties;
		this.methods = Collections.newSet();
		this.fields = Collections.newSet();
	}

	public Class(ClassId id, String name, Class parent, int properties, Iterable<? extends Attribute<?>> attributes) {
		this(id, name, parent, properties);

		for (Attribute<?> a : attributes) {
			addAttribute(a);
		}
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
		return name;
	}

	public int getProperties() {
		return properties;
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

	public List<? extends Attribute<?>> getAttributes() {
		List<Attribute<?>> attributes = Collections.newList();
		attributes.addAll(methods);
		attributes.addAll(fields);
		return attributes;
	}

	public List<AttributeId<?>> getAttributeIds() {
		List<AttributeId<?>> ids = Collections.newList();
		for (Attribute<?> a: getAttributes()) {
			ids.add(a.getId());
		}
		return ids;
	}

	public void addAttribute(Attribute<?> a) {
		if (a.getOwner() != this) throw new RuntimeException("trying to store unmatched attribute");
		if (attributeStorer == null) {
			attributeStorer = new AttributeVisitor() {
				public void visitMethod(Method method) {
					methods.add(method);
				}
				public void visitField(Field field) {
					fields.add(field);
				}
			};
		}

		a.visit(attributeStorer);
	}

	public List<Field> getFields() {
		return new ArrayList<Field>(fields);
	}

	public int getNumFields() {
		return fields.size();
	}

	public List<Method> getMethods() {
		return new ArrayList<Method>(methods);
	}

	public int getNumMethods() {
		return methods.size();
	}
}
