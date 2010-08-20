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
	}

	@Override
	public void visitInsn(int opcode) {
		switch (opcode) {
		case IRETURN:
		case LRETURN:
		case FRETURN:
		case DRETURN:
		case ARETURN:
		case RETURN:
			push(record.parent.id);
			push(record.id);
			visitInsn(ACONST_NULL);
			visitTrackerMethod(Tracker.exit);
			break;
		}

		super.visitInsn(opcode);
	}
}