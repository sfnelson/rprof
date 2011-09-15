/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class GenericClassWeaver extends ClassAdapter {

	protected final ClassRecord cr;

	protected boolean visitedCLInit = false;

	public GenericClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv);

		this.cr = cr;
	}

	@Override
	public void visitEnd() {
		if (!visitedCLInit) {
			cr.generateMethod("<clinit>", "()V", ACC_STATIC);
			MethodVisitor mv = visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		super.visitEnd();
	}

	@Override
	public void visit(int version, int access, String name, String signature,
					  String superName, String[] interfaces) {
		int major = version & 0xFFFF;
		//int minor = (version >> 16) & 0xFFFF;
		if (major < 49) {
			version = 49;
			cr.setProperties(cr.getProperties() | Clazz.CLASS_VERSION_UPDATED);
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, @NotNull String name, @NotNull String desc,
									 @Nullable String signature, @Nullable String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

		Method method = cr.getMethod(name, desc);

		if (method == null) {
			// we added it, so skip...
			return mv;
		}

		// check for: public static void main(String[])
		if (MethodUtils.isMain(method)) {
			mv = new MainMethodWeaver(cr, method, mv);
		}
		// check for <init>(..)
		else if (MethodUtils.isInit(method)) {
			mv = new InitMethodWeaver(cr, method, mv);
		}
		// check for: public boolean equals(Object)
		else if (MethodUtils.isEquals(method)) {
			mv = new EqualsMethodWeaver(cr, method, mv);
		}
		// check for: public int hashCode()
		else if (MethodUtils.isHashCode(method)) {
			mv = new HashCodeMethodWeaver(cr, method, mv);
		}
		// check for: <clinit>()
		else if (MethodUtils.isCLInit(method)) {
			visitedCLInit = true;
			mv = new CLInitMethodWeaver(cr, method, mv);
		}
		return mv;
	}

	protected MethodVisitor visitMethodRaw(int access, String name, String desc,
										   String signature, String[] exceptions) {
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
}
