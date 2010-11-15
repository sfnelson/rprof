package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LogData extends LogInfo<InstanceData, ClassData, MethodData, FieldData> implements IsSerializable {

	public long index;
	public long thread;
	public int event;
	public int cnum;
	public int mnum;
	public long[] args;
	
	public LogData() {}
	public LogData(long index, long thread, int event, int cnum, int mnum, long[] args) {
		this.index = index;
		this.thread = thread;
		this.event = event;
		this.cnum = cnum;
		this.mnum = mnum;
		this.args = args;
	}

	@Override
	public long[] getArguments() {
		return args;
	}
	
	@Override
	public int getClassNumber() {
		return cnum;
	}
	
	@Override
	public int getEvent() {
		return event;
	}
	
	@Override
	public long getIndex() {
		return index;
	}
	
	@Override
	public int getMethodNumber() {
		return mnum;
	}
	
	@Override
	public long getThread() {
		return thread;
	}
	
	public void visit(Visitor visitor) {
		DataManager.getInstance().visitLogEvent(this, visitor);
	}
		
	public interface Visitor {
		public void visitObjectAllocatedEvent(ObjectAllocated event);
		public void visitArrayAllocatedEvent(ArrayAllocated event);
		public void visitMethodEnterEvent(MethodEnter event);
		public void visitMethodReturnEvent(MethodReturn event);
		public void visitMethodExceptionEvent(MethodException event);
		public void visitFieldReadEvent(FieldRead event);
		public void visitFieldWriteEvent(FieldWrite event);
		public void visitClassWeaveEvent(ClassWeave event);
		public void visitClassInitializatedEvent(ClassInitialized event);
		public void visitObjectTaggedEvent(ObjectTagged event);
		public void visitObjectFreedEvent(ObjectFreed event);
	}

	public interface ObjectAllocated extends LogInfo.ObjectAllocated<InstanceData, ClassData, MethodData, FieldData> {}
	public interface ArrayAllocated extends LogInfo.ArrayAllocated<InstanceData, ClassData, MethodData, FieldData> {}
	public interface MethodEnter extends LogInfo.MethodEnter<InstanceData, ClassData, MethodData, FieldData> {}
	public interface MethodReturn extends LogInfo.MethodReturn<InstanceData, ClassData, MethodData, FieldData> {}
	public interface MethodException extends LogInfo.MethodException<InstanceData, ClassData, MethodData, FieldData> {}
	public interface FieldRead extends LogInfo.FieldRead<InstanceData, ClassData, MethodData, FieldData> {}
	public interface FieldWrite extends LogInfo.FieldWrite<InstanceData, ClassData, MethodData, FieldData> {}
	public interface ClassWeave extends LogInfo.ClassWeave<ClassData, MethodData, FieldData> {}
	public interface ClassInitialized extends LogInfo.ClassInitialized<ClassData, MethodData, FieldData> {}
	public interface ObjectTagged extends LogInfo.ObjectTagged<InstanceData, ClassData, MethodData, FieldData> {}
	public interface ObjectFreed extends LogInfo.ObjectFreed<InstanceData, ClassData, MethodData, FieldData> {}
}
