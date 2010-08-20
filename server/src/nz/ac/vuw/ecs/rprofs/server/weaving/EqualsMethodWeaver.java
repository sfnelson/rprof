/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Type;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class EqualsMethodWeaver extends MethodWeaver {

	public EqualsMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		push(record.parent.id);
		push(record.id);
		push(2);
		visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
		
		// store this
		visitInsn(DUP);
		push(0);
		visitVarInsn(ALOAD, 0);
		visitInsn(AASTORE);
		
		// store other
		visitInsn(DUP);
		push(0);
		visitVarInsn(ALOAD, 0);
		visitInsn(AASTORE);
		
		visitTrackerMethod(Tracker.enter);
	}

	@Override
	public void visitInsn(int code) {
		if (code == IRETURN) {
			push(record.parent.id);
			push(record.id);
			visitIntInsn(ALOAD, 0);
			visitTrackerMethod(Tracker.exit);
		}

		super.visitInsn(code);
	}
}
