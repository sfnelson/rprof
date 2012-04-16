package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.Collection;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/04/12
 */
public class AgentInitMethodWeaver extends MethodWeaver {

	public static final String NAME = "_rprof_agent_init";
	public static final String TYPE = "()V";

	public static void generate(ClassRecord cr, ClassAdapter ca) {
		cr.generateMethod(NAME, TYPE, ACC_STATIC);
		Method m = cr.getMethod(NAME, TYPE);
		MethodVisitor mv = ca.visitMethod(ACC_STATIC, NAME, TYPE, null, null);
		mv = new AgentInitMethodWeaver(cr, m, mv);
		mv.visitCode();
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private AgentInitMethodWeaver(ClassRecord record, Method method, MethodVisitor mv) {
		super(record, method, mv);
	}

	@Override
	public void visitCode() {
		super.visitCode();

		visitLdcInsn(Type.getType("L" + record.getName() + ";"));
		push(record.getId().getClassIndex());        // stack: cls, cid

		Collection<Field> watches = record.getWatches();
		if (watches.isEmpty()) {
			visitInsn(ACONST_NULL);                // stack: cls, cid, null
			setStack(3);
		} else {
			push(watches.size());                // stack: cls, cid, size
			visitIntInsn(NEWARRAY, T_INT);        // stack: cls, cid, fields
			setStack(3);

			int i = 0;
			for (Field f : watches) {
				dup();                            // stack: cls, cid, fields, fields
				push(i);                        // stack: cls, cid, fields, fields, i
				push(f.getId().getAttributeIndex());// stack: cls, cid, fields, fields, i, id
				visitInsn(IASTORE);                // stack: cls, cid, fields
				i++;
				setStack(6);
			}
		}
		visitTrackerMethod(Tracker.newcls);
		visitInsn(RETURN);
	}
}
