/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import org.objectweb.asm.MethodVisitor;

class MainMethodWeaver extends ExceptionHandlingMethodWeaver {

	public MainMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		super.visitCode();

		push(record.parent.getId());
		push(record.getId());
		visitTrackerMethod(Tracker.main);
		setStack(2);
	}

	@Override
	public void visitInsn(int opcode) {
		switch (opcode) {
		case DRETURN:
		case LRETURN:
			setStack(5);
		case IRETURN:
		case FRETURN:
		case ARETURN:
			setStack(4);
		case RETURN:
			push(record.getParent().getId());
			push(record.getId());
			visitInsn(ACONST_NULL);
			visitTrackerMethod(Tracker.exit);
			setStack(3);
			break;
		}

		super.visitInsn(opcode);
	}
}