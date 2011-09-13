/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import org.objectweb.asm.*;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class ThreadClassWeaver extends ClassAdapter {

	private final ClassRecord cr;

	public ThreadClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv);

		this.cr = cr;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
									 String signature, String[] exceptions) {
		MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);

		nz.ac.vuw.ecs.rprofs.server.domain.Method method = null;
		for (nz.ac.vuw.ecs.rprofs.server.domain.Method m : cr.getMethods().values()) {
			if (m.getName().equals(name)
					&& m.getDescription().equals(desc)
					&& m.getAccess() == access) {
				method = m;
			}
		}

		// check for <init>(..)
		if (MethodUtils.isInit(method)) {
			visitor = new ThreadInitMethodWeaver(cr, method, visitor);
		}
		if (MethodUtils.isCLInit(method)) {
			visitor = new CLInitMethodWeaver(cr, method, visitor);
		}
		return visitor;
	}

	@Override
	public void visitEnd() {
		Type t = Type.getType(HeapTracker.class);
		FieldVisitor fv = visitField(Opcodes.ACC_PUBLIC, "_rprof", t.getDescriptor(), null, null);
		if (fv != null) {
			fv.visitEnd();
		}

		super.visitEnd();
	}

	private static class ThreadInitMethodWeaver extends InitMethodWeaver {

		private boolean doOnce = true;

		public ThreadInitMethodWeaver(ClassRecord record,
									  nz.ac.vuw.ecs.rprofs.server.domain.Method method,
									  MethodVisitor visitor) {
			super(record, method, visitor);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			super.visitMethodInsn(opcode, owner, name, desc);

			//invokespecial   #40; //Method java/lang/Object."<init>":()V
			if (doOnce
					&& opcode == INVOKESPECIAL
					&& Type.getInternalName(Object.class).equals(owner)
					&& "<init>".equals(name)
					&& "()V".equals(desc)) {

				doOnce = false;

				// locals: [thread, ...]
				// stack:  []

				super.visitVarInsn(ALOAD, 0);

				// locals: [thread, ...]
				// stack:  [thread]

				visitTrackerMethod(Tracker.create);

				// locals: [thread, ...]
				// stack:  [thread, tracker]

				Type t = Type.getType(HeapTracker.class);
				super.visitFieldInsn(PUTFIELD, Type.getInternalName(Thread.class), "_rprof", t.getDescriptor());
			}
		}

	}
}
