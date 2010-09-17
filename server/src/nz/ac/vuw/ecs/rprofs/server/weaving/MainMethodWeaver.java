/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.MethodVisitor;

class MainMethodWeaver extends MethodWeaver {

	public MainMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		super.visitCode();
		push(record.parent.id);
		push(record.id);
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
			push(record.parent.id);
			push(record.id);
			visitInsn(ACONST_NULL);
			visitTrackerMethod(Tracker.exit);
			setStack(3);
			break;
		}

		super.visitInsn(opcode);
	}
}