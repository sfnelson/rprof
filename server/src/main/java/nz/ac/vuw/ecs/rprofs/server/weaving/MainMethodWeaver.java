/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.MethodVisitor;

class MainMethodWeaver extends ExceptionHandlingMethodWeaver {

	public MainMethodWeaver(ClassRecord record, Method method, MethodVisitor visitor) {
		super(record, method, visitor);
	}

	@Override
	public void visitCode() {
		super.visitCode();

		pushMethodReference(method);
		visitTrackerMethod(Tracker.main);
		setStack(2);
	}

	@Override
	public void visitInsn(int opcode) {
		switch (opcode) {
			case DRETURN:
			case LRETURN:
				setStack(5);
			case IRETURN:
			case FRETURN:
			case ARETURN:
				setStack(4);
			case RETURN:
				push(record.getId().indexValue());
				push(method.getId().attributeValue());
				visitInsn(ACONST_NULL);
				visitTrackerMethod(Tracker.exit);
				setStack(3);
				break;
		}

		super.visitInsn(opcode);
	}
}