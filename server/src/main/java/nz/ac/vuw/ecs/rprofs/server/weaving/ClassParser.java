package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import org.objectweb.asm.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class ClassParser extends ClassAdapter {

	private final ClassManager.ClassBuilder builder;

	public ClassParser(ClassManager.ClassBuilder builder) {
		super(null);
		this.builder = builder;
	}

	public ClassManager.ClassBuilder read(byte[] classfile) {
		new org.objectweb.asm.ClassReader(classfile)
				.accept(this, org.objectweb.asm.ClassReader.SKIP_CODE);
		return builder;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		builder.setName(name);
		builder.setParentName(superName);
	}

	@Override
	public void visitSource(String source, String debug) {
		// nothing to do.
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		// nothing to do.
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null; // nothing to do.
	}

	@Override
	public void visitAttribute(Attribute attr) {
		// nothing to do.
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		// nothing to do.
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		builder.addField()
				.setName(name)
				.setDescription(desc)
				.setAccess(access)
				.store();
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		builder.addMethod()
				.setName(name)
				.setDescription(desc)
				.setAccess(access)
				.store();
		return null;
	}

	@Override
	public void visitEnd() {
		// nothing to do.
	}
}
