package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 19/02/12
 */
public class CollectionMethodWeaver extends MethodWeaver {

	public CollectionMethodWeaver(ClassRecord record, Method method, MethodVisitor mv) {
		super(record, method, mv);
	}

	@Override
	public void visitCode() {
		super.visitCode();

		pushMethodReference(method);					// stack: cnum, mnum

		List<Integer> args = getArgs();

		if (args.size() > 1) {
			push(args.size());						// stack: cnum, mnum, numArgs
			visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
			// stack: cnum, mnum, args
			setStack(3);

			for (int i = 0; i < args.size(); i++) {
				visitInsn(DUP);							// stack: cnum, mnum, args, args
				push(i);								// stack: cnum, mnum, args, args, i
				visitVarInsn(ALOAD, args.get(i));		// stack: cnum, mnum, args, args, i, val
				visitInsn(AASTORE);						// stack: cnum, mnum, args
				setStack(6);
			}
		} else {
			visitInsn(ACONST_NULL);						// stack: cnum, mnum, null
			setStack(3);
		}

		visitTrackerMethod(Tracker.enter);
	}
}
