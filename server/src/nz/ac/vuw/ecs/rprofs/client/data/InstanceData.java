/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceData extends InstanceInfo<ClassData, MethodData, FieldData, LogData> implements IsSerializable {

	long id;
	ClassData type;
	MethodData constructor;
	LogData[] events;
	
	public InstanceData() {}
	public InstanceData(long id, ClassData type, MethodData constructor) {
		this.id = id;
		this.type = type;
		this.events = new LogData[0];
	}

	@Override
	public LogData[] getEvents() {
		return events;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public ClassData getType() {
		return type;
	}
	
	@Override
	public MethodData getConstructor() {
		return constructor;
	}
}
