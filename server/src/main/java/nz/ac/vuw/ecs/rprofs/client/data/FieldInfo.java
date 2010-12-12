/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public abstract class FieldInfo {

	public abstract int getId();
	public abstract String getName();
	public abstract String getDescription();
	public abstract int getAccess();
	public abstract boolean inEquals();
	public abstract boolean inHashCode();

	public FieldData toRPC() {
		return new FieldData(getId(), getName(), getDescription(), getAccess(), inEquals(), inHashCode());
	}

	public String toFieldString() {
		return getName() + ":" + getDescription();
	}

	public boolean isStatic() {
		return (8 & getAccess()) != 0; // Opcodes.ACC_STATIC = 8
	}
}
