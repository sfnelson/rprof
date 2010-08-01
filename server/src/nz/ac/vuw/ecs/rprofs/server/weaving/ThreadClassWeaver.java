/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;

import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.FieldVisitor;
import com.google.gwt.dev.asm.Opcodes;
import com.google.gwt.dev.asm.Type;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ThreadClassWeaver extends GenericClassWeaver {

	public ThreadClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv, cr);
	}

	@Override
	public void visitEnd() {
		Type t = Type.getType(HeapTracker.class);
		FieldVisitor fv = visitField(Opcodes.ACC_PUBLIC, "_rprof", t.getDescriptor(), null, null);
		if (fv != null) {
			fv.visitEnd();
		}
		
		super.visitEnd();
	}
}
