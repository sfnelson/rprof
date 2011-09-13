/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

public class TrackingClassWeaver extends ClassAdapter {

	private final ClassRecord record;

	public TrackingClassWeaver(ClassVisitor cv, ClassRecord record) {
		super(cv);

		this.record = record;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
									 String signature, String[] exceptions) {
		MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);

		Method method = null;
		for (Method m : record.getMethods().values()) {
			if (m.getName().equals(name)
					&& m.getDescription().equals(desc)
					&& m.getAccess() == access) {
				method = m;
			}
		}
		if (method == null) throw new RuntimeException("method was not found");

		if (name.equals("_getTracker")) {
			visitor = new GetTrackerGenerator(record, method, visitor);
		} else if (name.equals("_setTracker")) {
			visitor = new SetTrackerGenerator(record, method, visitor);
		}
		return visitor;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
								   String signature, Object value) {
		if (name.equals("cnum")) {
			return super.visitField(access, name, desc, signature,
					new Integer(record.getId().indexValue()));
		} else {
			return super.visitField(access, name, desc, signature, value);
		}
	}

	private static class GetTrackerGenerator extends GeneratorAdapter implements Opcodes {

		public GetTrackerGenerator(ClassRecord record, Method method, MethodVisitor visitor) {
			super(visitor, method.getAccess(), method.getName(), method.getDescription());
		}

		@Override
		public void visitCode() {
			super.visitCode();

			//return Thread.currentThread()._rprof;

			// locals: [thread]
			// stack:  []

			super.visitVarInsn(ALOAD, 0);

			// locals: [thread]
			// stack:  [thread]

			Type t = Type.getType(HeapTracker.class);
			super.visitFieldInsn(GETFIELD, Type.getInternalName(Thread.class), "_rprof", t.getDescriptor());

			// locals: [thread]
			// stack:  [tracker]

			super.visitInsn(ARETURN);

			super.visitMaxs(1, 1);

			super.visitEnd();
		}
	}

	private static class SetTrackerGenerator extends GeneratorAdapter implements Opcodes {

		public SetTrackerGenerator(ClassRecord record, Method method, MethodVisitor visitor) {
			super(visitor, method.getAccess(), method.getName(), method.getDescription());
		}

		@Override
		public void visitCode() {
			super.visitCode();

			// Thread.currentThread()._rprof = c; // c is first argument

			// locals: [thread, tracker]
			// stack:  []

			super.visitVarInsn(ALOAD, 0);

			// locals: [thread, tracker]
			// stack:  [thread]

			super.visitVarInsn(ALOAD, 1);

			// locals: [thread, tracker]
			// stack:  [thread, tracker]

			Type t = Type.getType(HeapTracker.class);
			super.visitFieldInsn(PUTFIELD, Type.getInternalName(Thread.class), "_rprof", t.getDescriptor());

			// locals: [thread, tracker]
			// stack:  []

			super.visitMaxs(2, 2);

			super.visitEnd();
		}
	}
}