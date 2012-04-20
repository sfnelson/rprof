package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.domain.MethodUtils;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/04/12
 */
abstract class BasicClassWeaver extends ClassAdapter {

	protected final ClassRecord cr;

	private boolean visitedCLInit = false;

	BasicClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv);

		this.cr = cr;
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
	public MethodVisitor visitMethod(int access, String name, String type, String sig, String[] ex) {
		MethodVisitor mv = super.visitMethod(access, name, type, sig, ex);
		Method m = cr.getMethod(name, type);

		if (m != null && MethodUtils.isCLInit(m)) {
			visitedCLInit = true;
			mv = new CLInitMethodWeaver(cr, m, mv);
		}

		return mv;
	}

	@Override
	public void visitEnd() {
		if ((cr.getProperties() & Clazz.GENERATED) == 0) {
			AgentInitMethodWeaver.generate(cr, this);
		}

		if (!visitedCLInit && (cr.getClazz().getAccess() & ACC_INTERFACE) == 0) {
			// CLInitMethodWeaver.generate(cr, this);
		}

		super.visitEnd();
	}
}
