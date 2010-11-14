/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public abstract class InstanceInfo<C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo, L extends LogInfo> {
	
	public abstract long getId();
	public abstract C getType();
	public abstract M getConstructor();
	public abstract L[] getEvents();
	
	public InstanceData toRPC() {
		InstanceData info = new InstanceData(getId(), getType().toRPC(), getConstructor().toRPC());
		info.events = new LogData[getEvents().length];
		int count = 0;
		for (L lr: getEvents()) {
			info.events[count++] = lr.toRPC();
		}
		return info;
	}
}
