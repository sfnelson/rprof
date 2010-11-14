/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public abstract class InstanceInfo<C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo> {
	
	public abstract long getId();
	public abstract C getType();
	
	public InstanceData toRPC() {
		if (this instanceof ExtendedInstanceInfo<?, ?, ?, ?, ?>) {
			ExtendedInstanceInfo<?, ?, ?, ?, ?> e = (ExtendedInstanceInfo<?, ?, ?, ?, ?>) this;
			LogData[] events = new LogData[e.getEvents().length];
			for (int i = 0; i < events.length; i++) {
				events[i] = e.getEvents()[i].toRPC();
			}
			return new ExtendedInstanceData(getId(), getType().toRPC(), e.getConstructor().toRPC(), events);
		}
		return new InstanceData(getId(), getType().toRPC());
	}
}
