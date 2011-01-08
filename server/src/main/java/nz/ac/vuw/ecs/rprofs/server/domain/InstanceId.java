/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.Embeddable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Embeddable
public class InstanceId implements IsSerializable, Comparable<InstanceId> {

	Long index;

	public InstanceId(long index) {
		this.index = index;
	}

	public InstanceId() {}

	@Override
	public int compareTo(InstanceId o) {
		if (index < o.index) return -1;
		if (index == o.index) return 0;
		return 1;
	}

	@Override
	public int hashCode() {
		return new Long(index).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (getClass().equals(o.getClass())) return false;
		InstanceId i = (InstanceId) o;
		return index == i.index;
	}

}
