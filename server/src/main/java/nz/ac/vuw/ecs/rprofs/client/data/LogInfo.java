/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import java.util.ArrayList;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public abstract class LogInfo implements Comparable<LogInfo> {

	public static final int OBJECT_ALLOCATED = 0x1;
	public static final int ARRAY_ALLOCATED = 0x2;
	public static final int METHOD_ENTER = 0x4;
	public static final int METHOD_RETURN = 0x8;
	public static final int FIELD_READ = 0x10;
	public static final int FIELD_WRITE = 0x20;
	public static final int CLASS_WEAVE = 0x40;
	public static final int CLASS_INITIALIZED = 0x80;
	public static final int OBJECT_TAGGED = 0x100;
	public static final int OBJECT_FREED = 0x200;
	public static final int METHOD_EXCEPTION = 0x400;

	public static final int ALL = 0xFFF;
	public static final int ALLOCATION = OBJECT_ALLOCATED | OBJECT_TAGGED;
	public static final int METHODS = METHOD_ENTER | METHOD_RETURN | METHOD_EXCEPTION;
	public static final int FIELDS = FIELD_READ | FIELD_WRITE;
	public static final int CLASSES = CLASS_WEAVE | CLASS_INITIALIZED;

	public abstract long getIndex();
	public abstract long getThread();
	public abstract int getEvent();
	public abstract int getClassNumber();
	public abstract int getMethodNumber();
	public abstract long[] getArguments();

	public LogData toRPC() {
		return new LogData(getIndex(), getThread(), getEvent(), getClassNumber(), getMethodNumber(), getArguments());
	}

	@Override
	public boolean equals(Object o) {
		if (!o.getClass().equals(this.getClass())) return false;
		LogInfo r = (LogInfo) o;
		return r.getIndex() == getIndex();
	}

	@Override
	public int hashCode() {
		return new Long(getIndex()).hashCode();
	}

	@Override
	public int compareTo(LogInfo o) {
		long d = getIndex() - o.getIndex();
		if (d < 0) return -1;
		else if (d > 0) return 1;
		return 0;
	}

	private interface Typed {
		ClassInfo getType();
	}

	private interface Target extends Typed {
		InstanceInfo getTarget();
	}

	private interface Field extends Target {
		FieldInfo getField();
	}

	private interface Method extends Target {
		MethodInfo getMethod();
	}

	public interface ObjectAllocated extends Target {
		MethodInfo getConstructor();
	}

	public interface ArrayAllocated extends Target {
		int getParameters();
	}

	public interface MethodEnter extends Method {
		ArrayList<? extends InstanceInfo> getParameters();
	}

	public interface MethodReturn extends Method {
		InstanceInfo getReturnValue();
	}

	public interface MethodException extends Method {
		InstanceInfo getThrowable();
	}

	public interface FieldRead extends Field {}
	public interface FieldWrite extends Field {
		InstanceInfo getValue();
	}

	public interface ClassWeave extends Typed {}
	public interface ClassInitialized extends Typed {}
	public interface ObjectTagged extends Target {}
	public interface ObjectFreed extends Target {}
}
