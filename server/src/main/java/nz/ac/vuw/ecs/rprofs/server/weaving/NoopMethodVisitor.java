package nz.ac.vuw.ecs.rprofs.server.weaving;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class NoopMethodVisitor implements MethodVisitor {
	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null;
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		return null;
	}

	@Override
	public void visitAttribute(Attribute attr) {
		// Do nothing.
	}

	@Override
	public void visitCode() {
		// Do nothing.
	}

	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
		// Do nothing.
	}

	@Override
	public void visitInsn(int opcode) {
		// Do nothing.
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		// Do nothing.
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		// Do nothing.
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		// Do nothing.
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// Do nothing.
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		// Do nothing.
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		// Do nothing.
	}

	@Override
	public void visitLabel(Label label) {
		// Do nothing.
	}

	@Override
	public void visitLdcInsn(Object cst) {
		// Do nothing.
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		// Do nothing.
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		// Do nothing.
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		// Do nothing.
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		// Do nothing.
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// Do nothing.
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		// Do nothing.
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		// Do nothing.
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		// Do nothing.
	}

	@Override
	public void visitEnd() {
		// Do nothing.
	}
}
