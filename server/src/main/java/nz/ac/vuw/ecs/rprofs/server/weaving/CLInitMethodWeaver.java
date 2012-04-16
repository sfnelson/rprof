/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class CLInitMethodWeaver extends MethodWeaver {

	public static final String NAME = "<clinit>";
	public static final String TYPE = "()V";
	public static final int ACCESS = ACC_STATIC;

	public static void generate(ClassRecord cr, ClassAdapter ca) {
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
	public void visitInsn(int insn) {
		if (false || insn == RETURN) {
			visitMethodInsn(INVOKESTATIC, record.getName(),
					AgentInitMethodWeaver.NAME, AgentInitMethodWeaver.TYPE);
		}

		super.visitInsn(insn);
	}
}
