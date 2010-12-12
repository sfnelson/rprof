/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.ActiveContext;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.FieldRecord;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class FieldReader implements ClassVisitor {

	private final ClassRecord record;
	private final ActiveContext context;

	public FieldReader(ActiveContext context, ClassRecord cr) {
		this.record = cr;
		this.context = context;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		context.initClassRecord(record, version, access, name, signature, superName, interfaces);
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
		FieldRecord fr = context.createFieldRecord(record);
		context.initFieldRecord(fr, access, name, desc);
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
