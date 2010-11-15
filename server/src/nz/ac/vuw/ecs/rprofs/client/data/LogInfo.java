/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import java.util.ArrayList;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public abstract class LogInfo<I extends InstanceInfo<C, M, F>,
C extends ClassInfo<C, M, F>,
M extends MethodInfo,
F extends FieldInfo> implements Comparable<LogInfo<?, ?, ?, ?>> {

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

	public boolean equals(Object o) {
		if (!o.getClass().equals(this.getClass())) return false;
		LogInfo<?, ?, ?, ?> r = (LogInfo<?, ?, ?, ?>) o;
		return r.getIndex() == getIndex();
	}

	public int hashCode() {
		return new Long(getIndex()).hashCode();
	}
	
	@Override
	public int compareTo(LogInfo<?, ?, ?, ?> o) {
		long d = getIndex() - o.getIndex();
		if (d < 0) return -1;
		else if (d > 0) return 1;
		return 0;
	}

	private interface Typed<C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	{
		C getType();
	}
	
	private interface Target<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Typed<C, M, F> {
		I getTarget();
	}
	
	private interface Field<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Target<I, C, M, F> {
		F getField();
	}
	
	private interface Method<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Target<I, C, M, F> {
		M getMethod();
	}

	protected interface ObjectAllocated<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Target<I, C, M, F>
	{
		M getConstructor();
	}

	protected interface ArrayAllocated<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Target<I, C, M, F>
	{
		int getParameters();
	}
	
	protected interface MethodEnter<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Method<I, C, M, F>
	{
		ArrayList<I> getParameters();
	}
	
	protected interface MethodReturn<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Method<I, C, M, F>
	{
		I getReturnValue();
	}
	
	protected interface MethodException<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Method<I, C, M, F>
	{
		I getThrowable();
	}
	
	protected interface FieldRead<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Field<I, C, M, F> {}
	
	protected interface FieldWrite<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Field<I, C, M, F>
	{
		I getValue();
	}
	
	protected interface ClassWeave<C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Typed<C, M, F> {}
	
	protected interface ClassInitialized<C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Typed<C, M, F> {}
	
	protected interface ObjectTagged<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Target<I, C, M, F> {}
	
	protected interface ObjectFreed<I extends InstanceInfo<C, M, F>, C extends ClassInfo<C, M, F>, M extends MethodInfo, F extends FieldInfo>
	extends Target<I, C, M, F> {}
}
