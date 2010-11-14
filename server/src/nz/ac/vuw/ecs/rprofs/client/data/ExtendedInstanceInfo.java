/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface ExtendedInstanceInfo<C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo, I extends InstanceInfo<C, M, F>, L extends LogInfo<I, C, M, F>>
{
	public long getId();
	public C getType();
	public M getConstructor();
	public L[] getEvents();
	
	public ExtendedInstanceData toRPC();
}
