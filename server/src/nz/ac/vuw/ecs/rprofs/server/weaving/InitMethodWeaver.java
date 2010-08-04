/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

import com.google.gwt.dev.asm.MethodVisitor;

class InitMethodWeaver extends MethodWeaver {

	public InitMethodWeaver(MethodVisitor mv, MethodRecord mr) {
		super(mv, mr);
	}

	@Override
	public void visitCode() {
		super.visitCode();
		push(record.parent.id);
		push(record.id);
		visitIntInsn(ALOAD, 0); // this
		visitTrackerMethod(Tracker.newobj);
	}
}