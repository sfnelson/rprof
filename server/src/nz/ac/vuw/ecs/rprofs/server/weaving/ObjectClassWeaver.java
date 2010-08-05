/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.ClassAdapter;
import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.MethodVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ObjectClassWeaver extends ClassAdapter {

	private final ClassRecord cr;

	public ObjectClassWeaver(ClassVisitor cv, ClassRecord cr) {
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

		if (name.equals("<init>")) {
			mv = new ObjectInitWeaver(mv, mr);
		}
		return mv;
	}

	private static class ObjectInitWeaver extends MethodWeaver {

		public ObjectInitWeaver(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr);
		}

		@Override
		public void visitCode() {
			super.visitCode();

			push(record.parent.id);
			push(record.id);
			visitIntInsn(ALOAD, 0); // this
			visitTrackerMethod(Tracker.newobj);
		}
	}
}
