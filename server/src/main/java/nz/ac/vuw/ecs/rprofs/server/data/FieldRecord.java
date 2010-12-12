/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import nz.ac.vuw.ecs.rprofs.client.data.FieldInfo;

import org.objectweb.asm.Opcodes;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table(name = "fields")
public class FieldRecord extends FieldInfo implements Comparable<FieldRecord>, Serializable {
	private static final long serialVersionUID = 2503578755127962360L;

	@Id @ManyToOne(cascade = CascadeType.MERGE)
	public ClassRecord parent;

	@Id public int id;
	public String name;
	public String desc;
	public int access;
	public boolean equals;
	public boolean hash;

	FieldRecord() {}

	FieldRecord(ClassRecord parent, int id) {
		this.parent = parent;
		this.id = id;

		parent.addField(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass().equals(this.getClass())) {
			FieldRecord fr = (FieldRecord) o;
			if (this.parent.equals(fr.parent)) {
				return fr.id == this.id;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public int compareTo(FieldRecord fr) {
		if (fr == null) return -1;
		if (this.parent.equals(fr.parent)) {
			return this.id - fr.id;
		}
		else {
			return this.parent.compareTo(fr.parent);
		}
	}

	void init(int access, String name, String desc) {
		this.name = name;
		this.desc = desc;
		this.access = access;
		if ((Opcodes.ACC_STATIC & access) == 0) {
			parent.addWatch(this);
		}
	}

	@Override
	public int getAccess() {
		return access;
	}

	@Override
	public String getDescription() {
		return desc;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean inEquals() {
		return equals;
	}

	@Override
	public boolean inHashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return parent + "." + name + ":" + desc;
	}

}
