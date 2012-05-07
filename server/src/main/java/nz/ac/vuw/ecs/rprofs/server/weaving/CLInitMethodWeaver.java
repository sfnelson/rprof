/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class CLInitMethodWeaver extends MethodWeaver {

	public static final String NAME = "<clinit>";
	public static final String TYPE = "()V";
	public static final int ACCESS = ACC_STATIC;

	public static void generate(ClassRecord cr, ClassVisitor ca) {
		cr.generateMethod(NAME, TYPE, ACCESS);
		MethodVisitor mv = ca.visitMethod(ACCESS, NAME, TYPE, null, null);
		mv.visitCode();
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	public CLInitMethodWeaver(ClassRecord cr, Method m, MethodVisitor mv) {
		super(cr, m, mv);
	}

	@Override
	public void visitCode() {
		super.visitCode();
		visitLdcInsn(Type.getType("L" + record.getName() + ";"));
		visitTrackerMethod(Tracker.clinit);
	}
}
