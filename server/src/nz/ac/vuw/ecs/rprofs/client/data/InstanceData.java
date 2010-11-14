/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceData extends InstanceInfo<ClassData, MethodData, FieldData> implements IsSerializable {

	private long id;
	private ClassData type;
	
	public InstanceData() {}
	public InstanceData(long id, ClassData type) {
		this.id = id;
		this.type = type;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public ClassData getType() {
		return type;
	}

}
