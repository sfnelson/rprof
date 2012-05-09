package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.domain.MethodUtils;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/04/12
 */
abstract class BasicClassWeaver extends ClassVisitor {

	protected final ClassRecord cr;

	BasicClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(Opcodes.ASM4, cv);

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
			/**
			 * There are several advantages to calling our profiler init method from <clinit>:
			 * * 'Primitive' classes do not generate ClassPrepare events but they do have <clinit> methods.
			 * * Calling a method causes <clinit> to run, allowing the class to be used before our method runs.
			 *   Inserting our init before regular <clinit> prevents events occurring before we're ready.
			 */
			mv = new CLInitMethodWeaver(cr, m, mv);
			cr.addProperty(Clazz.HAS_CLINIT);
		}

		return mv;
	}

	@Override
	public void visitEnd() {
		/**
		 * We could generate a <clinit> method here, but that has caused problems for some classes which do
		 * not expect one.
		 */
		/*
		if ((cr.getClazz().getAccess() & ACC_INTERFACE) == 0) {
			CLInitMethodWeaver.generate(cr, this);
		}*/

		if ((cr.getProperties() & Clazz.GENERATED_MATCHED) == 0) {
			AgentInitMethodWeaver.generate(cr, this);
			cr.addProperty(Clazz.HAS_RINIT);
		}

		super.visitEnd();
	}
}
