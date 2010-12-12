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
import javax.persistence.Transient;

import nz.ac.vuw.ecs.rprofs.client.data.MethodInfo;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table(name = "methods")
public class MethodRecord extends MethodInfo implements Comparable<MethodRecord>, Serializable {
	private static final long serialVersionUID = -357201240938009655L;

	@Id @ManyToOne(cascade = CascadeType.MERGE)
	public ClassRecord parent;

	@Id public int id;
	public String name;
	public String desc;
	public int access;

	@Transient
	public String signature;

	@Transient
	public String[] exceptions;

	MethodRecord() {}

	MethodRecord(ClassRecord parent, int id) {
		this.parent = parent;
		this.id = id;

		parent.addMethod(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass().equals(this.getClass())) {
			MethodRecord mr = (MethodRecord) o;
			if (this.parent.equals(mr.parent)) {
				return mr.id == this.id;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public int compareTo(MethodRecord mr) {
		if (parent.equals(mr.parent)) {
			return this.id - mr.id;
		}
		return parent.compareTo(mr.parent);
	}

	public void init(int access, String name, String desc, String signature, String[] exceptions) {
		this.access = access;
		this.name = name;
		this.desc = desc;
		this.signature = signature;
		this.exceptions = exceptions;
	}

	public ClassRecord getParent() {
		return parent;
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
	public String toString() {
		return parent + "." + name + ":" + desc;
	}
}
