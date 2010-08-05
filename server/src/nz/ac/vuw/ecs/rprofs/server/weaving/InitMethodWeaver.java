/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Type;

class InitMethodWeaver extends MethodWeaver {

	public InitMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		super.visitCode();

		List<Integer> args = getArgs();

		push(record.parent.id);											// stack: cnum
		push(record.id);												// stack: cnum, mnum
		push(args.size() - 1);												// stack: cnum, mnum, numArgs
		visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));	// stack: cnum, mnum, args

		// ignore 'this', it hasn't been initialized yet
		for (int i = 0; i < args.size() - 1; i++) {
			visitInsn(DUP);												// stack: cnum, mnum, args, args
			push(i);													// stack: cnum, mnum, args, args, i
			visitVarInsn(ALOAD, args.get(i + 1));						// stack: cnum, mnum, args, args, i, val
			visitInsn(AASTORE);											// stack: cnum, mnum, args
		}
		visitTrackerMethod(Tracker.enter);
	}

	@Override
	public void visitInsn(int code) {
		if (code == RETURN) {
			push(record.parent.id);											// stack: cnum
			push(record.id);												// stack: cnum, mnum
			visitTrackerMethod(Tracker.exit);
		}

		super.visitInsn(code);
	}
}