/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.Id;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class DatasetId implements IsSerializable {

	@Id
	String handle;

	public DatasetId() {}

	public DatasetId(String handle) {
		this.handle = handle;
	}

	public String getId() {
		return handle;
	}

	@Override
	public String toString() {
		return handle;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (!getClass().equals(o.getClass())) return false;
		DatasetId d = (DatasetId) o;
		return handle.equals(d.handle);
	}

	@Override
	public int hashCode() {
		return handle.hashCode();
	}
}
