/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.ClassAdapter;
import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Opcodes;
import com.google.gwt.dev.asm.Type;
import com.google.gwt.dev.asm.commons.GeneratorAdapter;

public class TrackingClassWeaver extends ClassAdapter {

	private ClassRecord record;

	public TrackingClassWeaver(ClassVisitor cv, ClassRecord record) {
		super(cv);

		this.record = record;
	}

	@Override
	public void visit(int version, int access, String name,
			String signature, String superName, String[] interfaces) {
		record.init(version, access, name, signature, superName, interfaces);
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		MethodRecord mr = MethodRecord.create(record, access, name, desc, signature, exceptions);
		if (name.equals("_getTracker")) {
			mv = new GetTrackerGenerator(mv, mr);
		}
		else if (name.equals("_setTracker")) {
			mv = new SetTrackerGenerator(mv, mr);
		}
		return mv;
	}

	private static class GetTrackerGenerator extends GeneratorAdapter implements Opcodes {

		public GetTrackerGenerator(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr.access, mr.name, mr.desc);
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

		public SetTrackerGenerator(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr.access, mr.name, mr.desc);
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