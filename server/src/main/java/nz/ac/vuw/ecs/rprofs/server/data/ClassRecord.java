/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.ClassInfo;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table( name = "classes" )
public class ClassRecord extends ClassInfo {

	@Id public int id;
	public String name;
	public int flags;

	@ManyToOne(optional = true)
	public ClassRecord parent;

	//@OneToMany(mappedBy = "parent")
	@Transient
	public List<MethodRecord> methods = Collections.newList();

	//@OneToMany(mappedBy = "parent")
	@Transient
	public List<FieldRecord> fields = Collections.newList();

	@Transient
	public String superName;

	@Transient
	public int version;

	@Transient
	public int access;

	@Transient
	public String signature;

	@Transient
	public String[] interfaces;

	ClassRecord() {}

	ClassRecord(int id) {
		this.id = id;
	}

	void init(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.version = version;
		this.access = access;
		this.name = name;
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
	}

	void link(Context context) {
		if (superName != null) {
			parent = context.getClass(superName);
		}
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	@Override
	public int getFlags() {
		return flags;
	}

	@Override
	public List<? extends MethodRecord> getMethods() {
		return Collections.immutable(methods);
	}

	@Override
	public List<? extends FieldRecord> getFields() {
		return Collections.immutable(fields);
	}

	@Override
	public ClassRecord getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return name;
	}

	@Transient
	private Set<FieldRecord> watches = Collections.newSet();

	public void addWatch(FieldRecord fr) {
		watches.add(fr);
	}

	public void removeWatch(FieldRecord field) {
		watches.remove(field);
	}

	public FieldRecord getField(String owner, String name, String desc) {
		if (!this.name.equals(owner)) {
			System.err.printf("%s doesn't know about %s's fields\n", this.name, owner);
			return null;
		}
		for (FieldRecord fr: getFields()) {
			if (fr.name.equals(name)) {
				if (fr.desc.equals(desc)) {
					return fr;
				}
				else {
					System.err.printf("%s doesn't match %s for %s.%s\n", desc, fr.desc, owner, name);
					return null;
				}
			}
		}
		System.err.printf("could not find %s.%s (%s)\n", owner, name, desc);
		return null;
	}

	void addMethod(MethodRecord method) {
		methods.add(method);
		Collections.sort(methods);
	}

	void addField(FieldRecord field) {
		fields.add(field);
		Collections.sort(fields);
	}

	public Collection<FieldRecord> getWatches() {
		return Collections.immutable(watches);
	}

	@Transient
	private ClassData rpc;

	@Override
	public ClassData toRPC() {
		if (rpc == null) {
			rpc = super.toRPC();
		}

		return rpc;
	}
}
