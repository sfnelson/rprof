/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.domain.MethodUtils;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class ObjectClassWeaver extends ClassAdapter {

	private final ClassRecord cr;

	public ObjectClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv);

		this.cr = cr;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
									 String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		Method method = cr.getMethod(name, desc);

		// check for <init>(..)
		if (MethodUtils.isInit(method)) {
			mv = new ObjectInitWeaver(cr, method, mv);
		}

		return mv;
	}

	private static class ObjectInitWeaver extends InitMethodWeaver {

		public ObjectInitWeaver(ClassRecord cr, Method m, MethodVisitor mv) {
			super(cr, m, mv);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			visitIntInsn(ALOAD, 0); // this
			visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
			visitIntInsn(ALOAD, 0); // this
			visitTrackerMethod(Tracker.newobj);
		}
	}
}
