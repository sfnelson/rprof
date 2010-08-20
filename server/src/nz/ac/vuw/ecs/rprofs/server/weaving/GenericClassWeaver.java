/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.ClassAdapter;
import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Opcodes;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class GenericClassWeaver extends ClassAdapter {

	private final ClassRecord cr;
	
	private boolean visitedCLInit = false;
	
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
			mv.visitEnd();
		}
		super.visitEnd();
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		cr.init(version, access, name, signature, superName, interfaces);
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		MethodRecord mr = MethodRecord.create(cr, access, name, desc, signature, exceptions);
		
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
}