/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Collection;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class CLInitMethodWeaver extends MethodWeaver {

	public CLInitMethodWeaver(ClassRecord cr, Method m, MethodVisitor mv) {
		super(cr, m, mv);
	}

	@Override
	public void visitCode() {
		super.visitCode();

		visitLdcInsn(Type.getType("L" + record.getName() + ";"));
		push(record.getId().getClassIndex());		// stack: cls, cid

		Collection<Field> watches = record.getWatches();
		if (watches.isEmpty()) {
			visitInsn(ACONST_NULL);				// stack: cls, cid, null
			setStack(3);
		} else {
			push(watches.size());				// stack: cls, cid, size
			visitIntInsn(NEWARRAY, T_INT);		// stack: cls, cid, fields
			setStack(3);

			int i = 0;
			for (Field f : watches) {
				dup();							// stack: cls, cid, fields, fields
				push(i);						// stack: cls, cid, fields, fields, i
				push(f.getId().getAttributeIndex());// stack: cls, cid, fields, fields, i, id
				visitInsn(IASTORE);				// stack: cls, cid, fields
				i++;
				setStack(6);
			}
		}
		visitTrackerMethod(Tracker.newcls);
	}
}
