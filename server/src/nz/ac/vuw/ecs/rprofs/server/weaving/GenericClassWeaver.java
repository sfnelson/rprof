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
	
	public GenericClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv);
		
		this.cr = cr;
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
		if ("main".equals(name) && "([Ljava/lang/String;)V".equals(desc)
				&& (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC) == access) {
			mv = new MainMethodWeaver(mv, mr);
		}
		// check for <init>(..)
		else if (name.equals("<init>")) {
			mv = new InitMethodWeaver(mv, mr);
		}
		return mv;
	}
}
