/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

class InitMethodWeaver extends MethodWeaver {

	private final Label start;
	private final Label end;
	private final Label handler;

	private int stacked = 0;
	private boolean supered = false;

	public InitMethodWeaver(ClassRecord record, Method method, MethodVisitor visitor) {
		super(record, method, visitor);

		start = new Label();
		end = new Label();
		handler = new Label();
	}

	@Override
	public void visitCode() {
		super.visitCode();

		pushMethodReference(method);                    // stack: cnum, mnum

		List<Integer> args = getArgs();

		if (args.size() > 1) {
			push(args.size() - 1);                        // stack: cnum, mnum, numArgs
			visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
			// stack: cnum, mnum, args
			setStack(3);

			// ignore 'this', it hasn't been initialized yet
			for (int i = 1; i < args.size(); i++) {
				visitInsn(DUP);                            // stack: cnum, mnum, args, args
				push(i - 1);                            // stack: cnum, mnum, args, args, i
				visitVarInsn(ALOAD, args.get(i));        // stack: cnum, mnum, args, args, i, val
				visitInsn(AASTORE);                        // stack: cnum, mnum, args
				setStack(6);
			}
		} else {
			visitInsn(ACONST_NULL);                        // stack: cnum, mnum, null
			setStack(3);
		}

		visitTrackerMethod(Tracker.enter);
	}

	@Override
	public void visitMethodInsn(int type, String cls, String mthd, String desc) {
		super.visitMethodInsn(type, cls, mthd, desc);
		if (type == INVOKESPECIAL && mthd.equals("<init>")) {
			//&& (cls.equals(record.getName()) || cls.equals(record.getClazz().getParentName()))
			//&& (mthd.equals("<init>"))
			//&& !supered) {
			if (stacked > 0) {
				stacked--;
			} else if (!supered) {
				visitLabel(start);
				supered = true;
			}
		}
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		super.visitTypeInsn(opcode, type);
		if (opcode == NEW) {
			stacked++;
		}
	}

	@Override
	public void visitInsn(int code) {
		if (code == RETURN) {
			pushMethodReference(method);                // stack: cnum, mnum
			visitVarInsn(ALOAD, 0);                        // stack: cnum, mnum, this
			visitTrackerMethod(Tracker.exit);
			setStack(3);
		}

		super.visitInsn(code);
	}

	@Override
	public void visitMaxs(int stack, int locals) {
		visitLabel(end);

		assert supered;

		visitTryCatchBlock(start, end, handler, Type.getInternalName(Throwable.class));

		visitLabel(handler);
		visitFrame(F_FULL,
				1, new Object[]{record.getName()},
				1, new Object[]{Type.getInternalName(Throwable.class)});

		visitVarInsn(ASTORE, 1); // store exception
		setLocals(2);

		pushMethodReference(method);
		visitVarInsn(ALOAD, 0);
		visitVarInsn(ALOAD, 1);
		visitTrackerMethod(Tracker.except);
		setStack(4);

		visitVarInsn(ALOAD, 1);
		visitInsn(ATHROW);

		super.visitMaxs(stack, locals);
	}
}