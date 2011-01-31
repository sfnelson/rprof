/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.events.ClassListHandler;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;

import com.google.gwt.core.client.GWT;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class DataManager implements ClassListHandler {

	private static final DataManager instance = GWT.create(DataManager.class);
	static {
		//Inspector.getInstance().addClassListHandler(instance);
	}

	public static DataManager getInstance() {
		return instance;
	}

	private Map<Integer, ClassData> classes = Collections.newMap();

	@Override
	public void onClassListAvailable(List<ClassData> classes) {
		this.classes.clear();
		for (ClassData c: classes) {
			this.classes.put(c.getId(), c);
		}
	}

	private ClassData getClassData(int cnum) {
		return classes.get(cnum);
	}

	private MethodData getMethodData(int cnum, int mnum) {
		ClassData c = getClassData(cnum);
		if (c == null) return null;
		return c.getMethods().get(mnum - 1);
	}

	private FieldData getFieldData(int cnum, int fnum) {
		ClassData c = getClassData(cnum);
		if (c == null) return null;
		return c.getFields().get(fnum - 1);
	}

	private LogData event;

	void visitLogEvent(LogData record, LogData.Visitor visitor) {
		event = record;

		switch (record.getEvent()) {
		case LogData.OBJECT_ALLOCATED:
			visitor.visitObjectAllocatedEvent(oa); break;
		case LogData.ARRAY_ALLOCATED:
			visitor.visitArrayAllocatedEvent(aa); break;
		case LogData.METHOD_ENTER:
			visitor.visitMethodEnterEvent(me); break;
		case LogData.METHOD_RETURN:
			visitor.visitMethodReturnEvent(mr); break;
		case LogData.METHOD_EXCEPTION:
			visitor.visitMethodExceptionEvent(mx); break;
		case LogData.FIELD_READ:
			visitor.visitFieldReadEvent(fr); break;
		case LogData.FIELD_WRITE:
			visitor.visitFieldWriteEvent(fw); break;
		case LogData.CLASS_INITIALIZED:
			visitor.visitClassInitializatedEvent(ci); break;
		case LogData.CLASS_WEAVE:
			visitor.visitClassWeaveEvent(cw); break;
		case LogData.OBJECT_TAGGED:
			visitor.visitObjectTaggedEvent(ot); break;
		case LogData.OBJECT_FREED:
			visitor.visitObjectFreedEvent(of); break;
		}
	}

	private class Typed {
		public ClassData getType() {
			return getClassData(event.cnum);
		}
	}

	private class Target extends Typed {
		public InstanceData getTarget() {
			return new InstanceData(event.args[0], getType());
		}
	}

	private class Field extends Target {
		public FieldData getField() {
			return getFieldData(event.cnum, event.mnum);
		}
	}

	private class Method extends Target {
		public MethodData getMethod() {
			return getMethodData(event.cnum, event.mnum);
		}
	}

	private ObjectAllocated oa = new ObjectAllocated();
	private class ObjectAllocated extends Target implements LogData.ObjectAllocated {
		@Override
		public MethodData getConstructor() {
			if (event.mnum != 0) {
				return getMethodData(event.cnum, event.mnum);
			}
			else {
				return null;
			}
		}
	}

	private ArrayAllocated aa = new ArrayAllocated();
	private class ArrayAllocated extends Target implements LogData.ArrayAllocated {
		@Override
		public int getParameters() {
			return (int) event.args[1];
		}
	}

	private MethodEnter me = new MethodEnter();
	private class MethodEnter extends Method implements LogData.MethodEnter {
		@Override
		public ArrayList<InstanceData> getParameters() {
			ArrayList<InstanceData> params = Collections.newList();
			for (int i = 1; i < event.args.length; i++) {
				if (event.args[i] == 0) params.add(null);
				else params.add(new InstanceData(event.args[i], null));
			}
			return params;
		}
	}

	private MethodReturn mr = new MethodReturn();
	private class MethodReturn extends Method implements LogData.MethodReturn {
		@Override
		public InstanceData getReturnValue() {
			if (event.args[1] == 0) return null;
			return new InstanceData(event.args[1], null);
		}
	}

	private MethodException mx = new MethodException();
	private class MethodException extends Method implements LogData.MethodException {
		@Override
		public InstanceData getThrowable() {
			if (event.args[1] == 0) return null;
			return new InstanceData(event.args[1], null);
		}
	}

	private FieldRead fr = new FieldRead();
	private class FieldRead extends Field implements LogData.FieldRead {}

	private FieldWrite fw = new FieldWrite();
	private class FieldWrite extends Field implements LogData.FieldWrite {
		@Override
		public InstanceData getValue() {
			if (event.args[1] == 0) return null;
			return new InstanceData(event.args[1], null);
		}
	}

	private ClassWeave cw = new ClassWeave();
	private class ClassWeave extends Typed implements LogData.ClassWeave {}

	private ClassInitialized ci = new ClassInitialized();
	private class ClassInitialized extends Typed implements LogData.ClassInitialized {}

	private ObjectTagged ot = new ObjectTagged();
	private class ObjectTagged extends Target implements LogData.ObjectTagged {}

	private ObjectFreed of = new ObjectFreed();
	private class ObjectFreed extends Target implements LogData.ObjectFreed {}
}
