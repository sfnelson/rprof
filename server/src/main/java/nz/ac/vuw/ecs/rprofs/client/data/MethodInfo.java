/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public abstract class MethodInfo {

	public abstract int getId();
	public abstract String getName();
	public abstract String getDescription();
	public abstract int getAccess();

	public MethodData toRPC() {
		return new MethodData(getId(), getName(), getDescription(), getAccess());
	}

	public boolean isNative() {
		return (0x800 & getAccess()) != 0;
	}

	public boolean isMain() {
		return "main".equals(getName()) && "([Ljava/lang/String;)V".equals(getDescription())
		&& (0x1 | 0x8) == getAccess(); // public, static
	}

	public boolean isInit() {
		return getName().equals("<init>");
	}

	public boolean isCLInit() {
		return getName().equals("<clinit>");
	}

	public boolean isEquals() {
		return "equals".equals(getName()) && "(Ljava/lang/Object;)Z".equals(getDescription())
		&& 0x1 == getAccess(); // public
	}

	public boolean isHashCode() {
		return "hashCode".equals(getName()) && "()I".equals(getDescription())
		&& 0x1 == getAccess(); // public
	}

	public boolean isStatic() {
		return (0x8 & getAccess()) != 0; // static
	}

	public String toMethodString() {
		return getName() + ":" + getDescription();
	}
}
