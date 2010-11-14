/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.server.data.Context.ActiveContext;

import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.MethodVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ObjectClassWeaver extends GenericClassWeaver {
	
	private final ActiveContext context;
	private final ClassRecord cr;
	
	public ObjectClassWeaver(ActiveContext context, ClassVisitor cv, ClassRecord cr) {
		super(context, cv, cr);
		
		this.context = context;
		this.cr = cr;
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
		MethodRecord mr = context.createMethodRecord(cr);
		context.initMethodRecord(mr, access, name, desc, signature, exceptions);

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
