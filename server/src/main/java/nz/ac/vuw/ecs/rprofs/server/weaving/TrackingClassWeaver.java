/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprof.HeapTracker;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class TrackingClassWeaver extends ClassAdapter {

	private final ClassRecord record;

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
		MethodRecord mr = record.weaver.createMethodRecord(name);
		mr.init(access, desc, signature, exceptions);

		if (name.equals("_getTracker")) {
			mv = new GetTrackerGenerator(mv, mr);
		}
		else if (name.equals("_setTracker")) {
			mv = new SetTrackerGenerator(mv, mr);
		}
		return mv;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		if (name.equals("cnum")) {
			return super.visitField(access, name, desc, signature, new Integer(record.id.getIndex()));
		}
		else {
			return super.visitField(access, name, desc, signature, value);
		}
	}

	private static class GetTrackerGenerator extends GeneratorAdapter implements Opcodes {

		public GetTrackerGenerator(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr.access, mr.name, mr.description);
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
			super(mv, mr.access, mr.name, mr.description);
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