/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceRecord implements IsSerializable {
	public long id;
	public int cnum;
	public int mnum;
}
