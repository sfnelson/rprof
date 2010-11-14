/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.Label;
import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Type;

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
		
		push(record.parent.getId());
		push(record.getId());
		visitVarInsn(ALOAD, 2);
		visitTrackerMethod(Tracker.except);
		setStack(3);
		
		visitVarInsn(ALOAD, 2);
		visitInsn(ATHROW);
		
		super.visitMaxs(stack, locals);
	}
}