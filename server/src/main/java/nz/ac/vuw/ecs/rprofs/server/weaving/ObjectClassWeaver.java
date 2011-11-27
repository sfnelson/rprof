/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.domain.MethodUtils;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class ObjectClassWeaver extends GenericClassWeaver {

	public ObjectClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv, cr);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
									 String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethodRaw(access, name, desc, signature, exceptions);
		Method method = cr.getMethod(name, desc);

		// check for <init>(..)
		if (MethodUtils.isInit(method)) {
			mv = new ObjectInitWeaver(cr, method, mv);
		}
		// check for: <clinit>()
		else if (MethodUtils.isCLInit(method)) {
			super.visitedCLInit = true;
			mv = new CLInitMethodWeaver(cr, method, mv);
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
			visitTrackerMethod(Tracker.newobj);
		}
	}
}
