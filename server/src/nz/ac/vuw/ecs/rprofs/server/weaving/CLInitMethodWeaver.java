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
public class CLInitMethodWeaver extends MethodWeaver {

	public CLInitMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}
	
	public void visitCode() {
		visitLdcInsn(Type.getType("L" + record.parent.name + ";"));
		push(record.parent.id);
		visitTrackerMethod(Tracker.newcls);
	}
}
