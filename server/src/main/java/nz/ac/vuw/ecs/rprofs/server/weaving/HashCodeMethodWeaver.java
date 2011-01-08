/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class HashCodeMethodWeaver extends ExceptionHandlingMethodWeaver {

	public HashCodeMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		super.visitCode();

		push(record.parent.id);		// stack: cid
		push(record.id);			// stack: cid, mid
		push(1);					// stack: cid, mid, 1
		visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
		// stack: cid, mid, args
		// store this
		visitInsn(DUP);				// stack: cid, mid, args, args
		push(0);					// stack: cid, mid, args, args, 0
		visitVarInsn(ALOAD, 0);		// stack: cid, mid, args, args, 0, this
		visitInsn(AASTORE);			// stack: cid, mid, args

		visitTrackerMethod(Tracker.enter);

		setStack(6);
	}

	@Override
	public void visitInsn(int code) {
		if (code == IRETURN) {
			push(record.parent.id);
			push(record.id);
			visitIntInsn(ALOAD, 0);
			visitTrackerMethod(Tracker.exit);

			setStack(4); // ret, cid, mid, this
		}

		super.visitInsn(code);
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		//record.parent.addWatch(owner, name, desc);
		Field fr = record.weaver.getField(owner, name, desc);
		if (fr != null) record.weaver.setHash(fr, true);
		super.visitFieldInsn(opcode, owner, name, desc);
	}
}
