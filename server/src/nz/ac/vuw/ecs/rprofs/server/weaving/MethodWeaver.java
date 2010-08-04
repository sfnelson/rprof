/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.lang.reflect.Method;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Opcodes;
import com.google.gwt.dev.asm.Type;
import com.google.gwt.dev.asm.commons.GeneratorAdapter;

public class MethodWeaver extends GeneratorAdapter implements Opcodes {

	protected final MethodRecord record;

	public MethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr.access, mr.name, mr.desc);

		this.record = mr;
	}
	
	protected void visitTrackerMethod(Method m) {
		visitMethodInsn(INVOKESTATIC, Tracker.getName(), m.getName(), Type.getMethodDescriptor(m));
	}
	
	protected static class Tracker {
		public static final Class<HeapTracker> cls = HeapTracker.class;
		public static final Method enter = getTrackerMethod("enter");
		public static final Method exit = getTrackerMethod("exit");
		public static final Method newarr = getTrackerMethod("newarr");
		public static final Method newobj = getTrackerMethod("newobj");
		public static final Method main = getTrackerMethod("main");

		public static String getName() {
			return Type.getInternalName(cls);
		}
	}

	private static Method getTrackerMethod(String name) {
		try {
			Method[] methods = Tracker.cls.getMethods();
			Method e = null;
			for (Method m: methods) {
				if (m.getName() == name) {
					e = m;
					break;
				}
			}
			if (e == null) {
				throw new NoSuchMethodException();
			} else {
				return e;
			}
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}