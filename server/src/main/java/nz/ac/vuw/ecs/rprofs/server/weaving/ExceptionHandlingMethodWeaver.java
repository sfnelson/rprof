/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
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

		visitTryCatchBlock(start, end, handler, Type.getInternalName(Exception.class));

		visitLabel(handler);
		//visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { Type.getInternalName(Exception.class) });
		visitFrame(Opcodes.F_FULL, 0, new Object[] {}, 1, new Object[] { Type.getInternalName(Exception.class) });

		visitVarInsn(ASTORE, 1); // store exception
		setLocals(2);

		push(record.parent.id.indexValue());
		push(record.id);
		visitVarInsn(ALOAD, 1);
		visitTrackerMethod(Tracker.except);
		setStack(3);

		visitVarInsn(ALOAD, 1);
		visitInsn(ATHROW);

		super.visitMaxs(stack, locals);
	}
}
