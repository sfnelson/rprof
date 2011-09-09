/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import org.objectweb.asm.*;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class FieldReader implements ClassVisitor {

	private final ClassRecord record;

	public FieldReader(ClassRecord cr) {
		this.record = cr;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
					  String superName, String[] interfaces) {
		record.init(version, access, name, signature, superName, interfaces);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null;
	}

	@Override
	public void visitAttribute(Attribute attr) {
		// Nothing to do.
	}

	@Override
	public void visitEnd() {
		// Nothing to do.
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
								   String signature, Object value) {
		FieldRecord fr = record.weaver.createFieldRecord(name);
		fr.init(access, desc);
		return null;
	}

	@Override
	public void visitInnerClass(String name, String outerName,
								String innerName, int access) {
		// Nothing to do.
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
									 String signature, String[] exceptions) {
		// Ignore methods
		return null;
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		// Nothing to do here
	}

	@Override
	public void visitSource(String source, String debug) {
		// This visitor ignores source
	}
}
