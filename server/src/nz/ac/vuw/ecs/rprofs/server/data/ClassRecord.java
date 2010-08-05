/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ClassRecord extends nz.ac.vuw.ecs.rprofs.client.data.ClassRecord<MethodRecord> {
	private static final long serialVersionUID = 5868120036712274141L;

	public int version;
	public int access;
	public String signature;
	public String superName;
	public String[] interfaces;

	public int id() {
		return id;
	}
	
	public void init(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.version = version;
		this.access = access;
		this.name = name;
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
		this.instances = 0;
	}
	
	public String toString() {
		return "c:" + name;
	}
}
