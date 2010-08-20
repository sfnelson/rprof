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
public class HashCodeMethodWeaver extends MethodWeaver {

	public HashCodeMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		push(record.parent.id);
		push(record.id);
		push(1);
		visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
		
		// store this
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
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		switch (opcode) {
		case GETSTATIC:
		case GETFIELD:
			System.out.println(record.name + ": " + desc + " " + owner + "." + name);
			break;
		}
		
		super.visitFieldInsn(opcode, owner, name, desc);
	}
}
