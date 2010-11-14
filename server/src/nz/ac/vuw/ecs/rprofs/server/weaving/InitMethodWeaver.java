/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Type;

class InitMethodWeaver extends ExceptionHandlingMethodWeaver {

	public InitMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		super.visitCode();

		List<Integer> args = getArgs();

		push(record.parent.getId());									// stack: cnum
		push(record.getId());											// stack: cnum, mnum

		if (args.size() > 1) {
			push(args.size() - 1);										// stack: cnum, mnum, numArgs
			visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));// stack: cnum, mnum, args
			setStack(3);

			// ignore 'this', it hasn't been initialized yet
			for (int i = 0; i < args.size() - 1; i++) {
				visitInsn(DUP);											// stack: cnum, mnum, args, args
				push(i);												// stack: cnum, mnum, args, args, i
				visitVarInsn(ALOAD, args.get(i + 1));					// stack: cnum, mnum, args, args, i, val
				visitInsn(AASTORE);										// stack: cnum, mnum, args
				setStack(6);
			}
		}
		else {
			visitInsn(ACONST_NULL);										// stack: cnum, mnum, null
			setStack(3);
		}

		visitTrackerMethod(Tracker.enter);
	}

	@Override
	public void visitInsn(int code) {
		if (code == RETURN) {
			push(record.parent.getId());								// stack: cnum
			push(record.getId());										// stack: cnum, mnum
			visitIntInsn(ALOAD, 0);										// stack: cnum, mnum, this
			visitTrackerMethod(Tracker.exit);
			setStack(3);
		}

		super.visitInsn(code);
	}
}