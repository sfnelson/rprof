/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.Collection;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class CLInitMethodWeaver extends MethodWeaver {

	public CLInitMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		visitLdcInsn(Type.getType("L" + record.parent.name + ";"));
		push(record.parent.id);			// stack: cls, cid

		Collection<FieldRecord> watches = record.parent.watches;
		if (watches.isEmpty()) {
			visitInsn(ACONST_NULL);				// stack: cls, cid, null
			setStack(3);
		}
		else {
			push(watches.size());				// stack: cls, cid, size
			visitIntInsn(NEWARRAY, T_INT);		// stack: cls, cid, fields
			setStack(3);

			int i = 0;
			for (FieldRecord f: watches) {
				dup();							// stack: cls, cid, fields, fields
				push(i);						// stack: cls, cid, fields, fields, i
				push(f.id);						// stack: cls, cid, fields, fields, i, id
				visitInsn(IASTORE);				// stack: cls, cid, fields
				i++;
				setStack(6);
			}
		}
		visitTrackerMethod(Tracker.newcls);
	}
}
