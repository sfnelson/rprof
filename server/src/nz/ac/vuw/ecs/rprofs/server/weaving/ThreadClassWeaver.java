/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.server.data.Context.ActiveContext;

import com.google.gwt.dev.asm.ClassAdapter;
import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.FieldVisitor;
import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Opcodes;
import com.google.gwt.dev.asm.Type;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ThreadClassWeaver extends ClassAdapter {

	private final ActiveContext context;
	private final ClassRecord cr;
	
	public ThreadClassWeaver(ActiveContext context, ClassVisitor cv, ClassRecord cr) {
		super(cv);
		
		this.context = context;
		this.cr = cr;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		context.initClassRecord(cr, version, access, name, signature, superName, interfaces);
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		MethodRecord mr = context.createMethodRecord(cr);
		context.initMethodRecord(mr, access, name, desc, signature, exceptions);
		
		// check for <init>(..)
		if (mr.isInit()) {
			mv = new ThreadInitMethodWeaver(mv, mr);
		}
		if (mr.isCLInit()) {
			mv = new CLInitMethodWeaver(mv, mr);
		}
		return mv;
	}

	@Override
	public void visitEnd() {
		Type t = Type.getType(HeapTracker.class);
		FieldVisitor fv = visitField(Opcodes.ACC_PUBLIC, "_rprof", t.getDescriptor(), null, null);
		if (fv != null) {
			fv.visitEnd();
		}
		
		super.visitEnd();
	}
	
	private static class ThreadInitMethodWeaver extends InitMethodWeaver {

		private boolean doOnce = true;
		
		public ThreadInitMethodWeaver(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr);
		}
		
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			super.visitMethodInsn(opcode, owner, name, desc);
			
			//invokespecial   #40; //Method java/lang/Object."<init>":()V
			if (doOnce
					&& opcode == INVOKESPECIAL
					&& Type.getInternalName(Object.class).equals(owner)
					&& "<init>".equals(name)
					&& "()V".equals(desc)) {
				
				doOnce = false;
				
				// locals: [thread, ...]
				// stack:  []

				super.visitVarInsn(ALOAD, 0);

				// locals: [thread, ...]
				// stack:  [thread]
				
				visitTrackerMethod(Tracker.create);

				// locals: [thread, ...]
				// stack:  [thread, tracker]

				Type t = Type.getType(HeapTracker.class);
				super.visitFieldInsn(PUTFIELD, Type.getInternalName(Thread.class), "_rprof", t.getDescriptor());
			}
		}
		
	}
}
