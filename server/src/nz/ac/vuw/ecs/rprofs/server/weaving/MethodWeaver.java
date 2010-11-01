/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Opcodes;
import com.google.gwt.dev.asm.Type;
import com.google.gwt.dev.asm.commons.GeneratorAdapter;

public class MethodWeaver extends GeneratorAdapter implements Opcodes {

	protected final MethodRecord record;
	
	protected int maxStack = 0;
	protected int maxLocals = 0;

	public MethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr.access, mr.name, mr.desc);
		this.record = mr;
	}
	
	protected void setStack(int stack) {
		maxStack = Math.max(maxStack, stack);
	}
	
	protected void setLocals(int locals) {
		maxLocals = Math.max(maxLocals, locals);
	}
	
	public void visitMaxs(int stack, int locals) {
		setStack(stack);
		setLocals(locals);
		super.visitMaxs(maxStack, maxLocals);
	}
	
	protected void visitTrackerMethod(Method m) {
		visitMethodInsn(INVOKESTATIC, Tracker.getName(), m.getName(), Type.getMethodDescriptor(m));
	}
	
	protected List<Integer> getArgs() {
		List<Integer> useful = new ArrayList<Integer>(); 
		int argIndex = 0;

		// mark 'this'
		useful.add(0);
		argIndex++;

		outer:
			for (int i = 0; i < record.desc.length(); i++) {
				switch(record.desc.charAt(i)) {
				case ')': break outer;
				case 'J': // long
				case 'D': // double
					argIndex++;
				case 'B': // byte
				case 'C': // character
				case 'S': // short
				case 'Z': // boolean
				case 'F': // float
				case 'I': // int
					argIndex++;
					break;
				case 'L': // object, followed by FQ name, terminated by ';'
				case '[': // array, followed by a second type descriptor
					useful.add(argIndex++);
					while (record.desc.charAt(i) == '[') i++;
					if (record.desc.charAt(i) == 'L') {
						while (record.desc.charAt(++i) != ';');
					}
					break;
				}
			}

		return useful;
	}
	
	protected static class Tracker {
		public static final Class<HeapTracker> cls = HeapTracker.class;
		public static final Method enter = getTrackerMethod("enter");
		public static final Method exit = getTrackerMethod("exit");
		public static final Method except = getTrackerMethod("except");
		public static final Method newarr = getTrackerMethod("newarr");
		public static final Method newobj = getTrackerMethod("newobj");
		public static final Method newcls = getTrackerMethod("newcls");
		public static final Method main = getTrackerMethod("main");
		public static final Method create = getTrackerMethod("create");

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