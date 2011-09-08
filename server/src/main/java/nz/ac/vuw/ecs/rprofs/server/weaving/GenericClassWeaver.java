/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
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
			MethodVisitor mv = visitMethod(0, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(Opcodes.RETURN);
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
			cr.setProperties(cr.properties | Clazz.CLASS_VERSION_UPDATED);
		}
		cr.init(version, access, name, signature, superName, interfaces);
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, @NotNull String name, @NotNull String desc,
			@Nullable String signature, @Nullable String[] exceptions) {
		//MethodVisitor mv = new AnalyzerAdapter(cr.name, access, name, desc,
		//		super.visitMethod(access, name, desc, signature, exceptions));
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		MethodRecord mr = cr.weaver.createMethodRecord(name);
		mr.init(access, desc, signature, exceptions);

		// check for: public static void main(String[])
		if (mr.isMain()) {
			mv = new MainMethodWeaver(mv, mr);
		}
		// check for <init>(..)
		else if (mr.isInit()) {
			mv = new InitMethodWeaver(mv, mr);
		}
		// check for: public boolean equals(Object)
		else if (mr.isEquals()) {
			mv = new EqualsMethodWeaver(mv, mr);
		}
		// check for: public int hashCode()
		else if (mr.isHashCode()) {
			mv = new HashCodeMethodWeaver(mv, mr);
		}
		// check for: <clinit>()
		else if (mr.isCLInit()) {
			visitedCLInit = true;
			mv = new CLInitMethodWeaver(mv, mr);
		}
		return mv;
	}

	protected MethodVisitor visitMethodRaw(int access, String name, String desc,
			String signature, String[] exceptions) {
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
}
