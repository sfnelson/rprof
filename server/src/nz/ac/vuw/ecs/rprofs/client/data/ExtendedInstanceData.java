/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ExtendedInstanceData extends InstanceData
implements ExtendedInstanceInfo<ClassData, MethodData, FieldData, InstanceData, LogData>, IsSerializable {

	private MethodData constructor;
	private LogData[] events;
	
	public ExtendedInstanceData() {}
	public ExtendedInstanceData(long id, ClassData type, MethodData constructor, LogData[] events) {
		super(id, type);
		this.constructor = constructor;
		this.events = events;
	}

	@Override
	public LogData[] getEvents() {
		return events;
	}
	
	@Override
	public MethodData getConstructor() {
		return constructor;
	}
	
	public ExtendedInstanceData toRPC() {
		return new ExtendedInstanceData(getId(), getType(), constructor, events);
	}
}
