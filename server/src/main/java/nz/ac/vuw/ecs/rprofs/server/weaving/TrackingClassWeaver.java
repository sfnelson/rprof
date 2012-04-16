/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import static org.objectweb.asm.Opcodes.*;

public class TrackingClassWeaver extends BasicClassWeaver {

	public TrackingClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv, cr);

		cr.getWatches().clear();
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
									 String signature, String[] exceptions) {
		MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);

		Method method = cr.getMethod(name, desc);
		if (method == null) throw new RuntimeException("method was not found");

		if (name.equals("_getTracker")) {
			visitor = new GetTrackerGenerator(method, visitor);
		} else if (name.equals("_setTracker")) {
			visitor = new SetTrackerGenerator(method, visitor);
		}
		return visitor;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
								   String signature, Object value) {
		if (name.equals("cnum")) {
			return super.visitField(access, name, desc, signature,
					new Integer(cr.getId().getClassIndex()));
		} else {
			return super.visitField(access, name, desc, signature, value);
		}
	}

	private static class GetTrackerGenerator extends NoopMethodVisitor {

		private final GeneratorAdapter visitor;

		public GetTrackerGenerator(Method method, MethodVisitor visitor) {
			this.visitor = new GeneratorAdapter(visitor, method.getAccess(), method.getName(), method.getDescription());
		}

		@Override
		public void visitCode() {
			visitor.visitCode();

			//return Thread.currentThread()._rprof;

			// locals: [thread]
			// stack:  []

			visitor.visitVarInsn(ALOAD, 0);

			// locals: [thread]
			// stack:  [thread]

			Type t = Type.getType(HeapTracker.class);
			visitor.visitFieldInsn(GETFIELD, Type.getInternalName(Thread.class), "_rprof", t.getDescriptor());

			// locals: [thread]
			// stack:  [tracker]

			visitor.visitInsn(ARETURN);

			visitor.visitMaxs(1, 1);

			visitor.visitEnd();
		}
	}

	private static class SetTrackerGenerator extends NoopMethodVisitor {

		private final GeneratorAdapter visitor;

		public SetTrackerGenerator(Method method, MethodVisitor visitor) {
			this.visitor = new GeneratorAdapter(visitor, method.getAccess(), method.getName(), method.getDescription());
		}

		@Override
		public void visitCode() {
			visitor.visitCode();

			// Thread.currentThread()._rprof = c; // c is first argument

			// locals: [thread, tracker]
			// stack:  []

			visitor.visitVarInsn(ALOAD, 0);

			// locals: [thread, tracker]
			// stack:  [thread]

			visitor.visitVarInsn(ALOAD, 1);

			// locals: [thread, tracker]
			// stack:  [thread, tracker]

			Type t = Type.getType(HeapTracker.class);
			visitor.visitFieldInsn(PUTFIELD, Type.getInternalName(Thread.class), "_rprof", t.getDescriptor());

			// locals: [thread, tracker]
			// stack:  []

			visitor.visitInsn(RETURN);

			visitor.visitMaxs(2, 2);

			visitor.visitEnd();
		}
	}
}