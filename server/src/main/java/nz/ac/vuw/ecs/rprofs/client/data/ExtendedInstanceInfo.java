/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface ExtendedInstanceInfo {
	public long getId();
	public ClassInfo getType();
	public MethodInfo getConstructor();
	public LogInfo[] getEvents();

	public ExtendedInstanceData toRPC();
}
