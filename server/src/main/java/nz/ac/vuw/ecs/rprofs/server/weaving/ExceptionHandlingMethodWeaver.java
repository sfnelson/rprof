/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ExceptionHandlingMethodWeaver extends MethodWeaver {

	private final Label start;
	private final Label end;
	private final Label handler;

	public ExceptionHandlingMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);

		start = new Label();
		end = new Label();
		handler = new Label();
	}

	@Override
	public void visitCode() {
		super.visitCode();
		visitLabel(start);
	}

	@Override
	public void visitMaxs(int stack, int locals) {
		visitLabel(end);
		visitLabel(handler);
		visitTryCatchBlock(start, end, handler, Type.getInternalName(Exception.class));

		visitVarInsn(ASTORE, 2);
		setLocals(3);

		push(record.parent.id.getIndex());
		push(record.id);
		visitVarInsn(ALOAD, 2);
		visitTrackerMethod(Tracker.except);
		setStack(3);

		visitVarInsn(ALOAD, 2);
		visitInsn(ATHROW);

		super.visitMaxs(stack, locals);
	}
}
