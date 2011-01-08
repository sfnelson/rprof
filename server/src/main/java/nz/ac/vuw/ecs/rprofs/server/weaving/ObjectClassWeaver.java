/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ObjectClassWeaver extends GenericClassWeaver {

	public ObjectClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv, cr);
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethodRaw(access, name, desc, signature, exceptions);
		MethodRecord mr = cr.weaver.createMethodRecord(name);
		mr.init(access, desc, signature, exceptions);

		// check for <init>(..)
		if (mr.isInit()) {
			mv = new ObjectInitWeaver(mv, mr);
		}
		// check for: <clinit>()
		else if (mr.isCLInit()) {
			super.visitedCLInit = true;
			mv = new CLInitMethodWeaver(mv, mr);
		}

		return mv;
	}

	private static class ObjectInitWeaver extends InitMethodWeaver {

		public ObjectInitWeaver(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			visitIntInsn(ALOAD, 0); // this
			visitTrackerMethod(Tracker.newobj);
		}
	}
}
