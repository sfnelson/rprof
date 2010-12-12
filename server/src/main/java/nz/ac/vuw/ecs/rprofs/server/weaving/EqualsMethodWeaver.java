/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class EqualsMethodWeaver extends ExceptionHandlingMethodWeaver {

	public EqualsMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		super.visitCode();
		
		push(record.parent.getId());// stack: cid
		push(record.getId());		// stack: cid, mid
		push(2);					// stack: cid, mid, 2
		visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
									// stack: cid, mid, args
		// store this
		visitInsn(DUP);				// stack: cid, mid, args, args
		push(0);					// stack: cid, mid, args, args, 0
		visitVarInsn(ALOAD, 0);		// stack: cid, mid, args, args, 0, this
		visitInsn(AASTORE);			// stack: cid, mid, args
		
		// store other
		visitInsn(DUP);				// stack: cid, mid, args, args
		push(1);					// stack: cid, mid, args, args, 1
		visitVarInsn(ALOAD, 1);		// stack: cid, mid, args, args, 1, other
		visitInsn(AASTORE);			// stack: cid, mid, args
		
		visitTrackerMethod(Tracker.enter);
		
		setStack(6);
	}

	@Override
	public void visitInsn(int code) {
		if (code == IRETURN) {
			push(record.parent.getId());
			push(record.getId());
			visitIntInsn(ALOAD, 0);
			visitTrackerMethod(Tracker.exit);
			
			setStack(4); // ret, cid, mid, this
		}

		super.visitInsn(code);
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		//record.parent.addWatch(owner, name, desc);
		FieldRecord fr = record.parent.getField(owner, name, desc);
		if (fr != null) fr.equals = true;
		super.visitFieldInsn(opcode, owner, name, desc);
	}
}
