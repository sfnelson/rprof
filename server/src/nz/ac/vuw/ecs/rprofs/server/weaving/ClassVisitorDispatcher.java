/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import com.google.gwt.dev.asm.AnnotationVisitor;
import com.google.gwt.dev.asm.Attribute;
import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.FieldVisitor;
import com.google.gwt.dev.asm.MethodVisitor;

/**
 * Dispatches class visitor calls to the current class visitor, allowing
 * implementations to be swapped based on the original call to visit.
 * 
 * Classes extending this class should overwrite the {@link visit} method
 * to decide on an implementation to use, then set that implementation as
 * the one to dispatch to using the {@link setClassVisitor} method.
 * 
 * @author Stephen Nelson (stephen@sfnelson.org)
 * 
 */
public abstract class ClassVisitorDispatcher implements ClassVisitor {

	private ClassVisitor cv;

	/**
	 * Set the class visitor to use for this class. This method must be called
	 * from the {@link visit} method.
	 */
	protected final void setClassVisitor(ClassVisitor cv) {
		this.cv = cv;
	}

	/**
	 * Classes defining this method should create an appropriate class visitor,
	 * set it using the {@link setClassVisitor} method, then call {@link visit}
	 * on the new visitor.
	 */
	@Override
	public abstract void visit(int version, int access, String name,
			String signature, String superName, String[] interfaces);

	@Override
	public final AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return cv.visitAnnotation(desc, visible);
	}

	@Override
	public final void visitAttribute(Attribute attr) {
		cv.visitAttribute(attr);
	}

	@Override
	public final void visitEnd() {
		cv.visitEnd();
	}

	@Override
	public final FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		return cv.visitField(access, name, desc, signature, value);
	}

	@Override
	public final void visitInnerClass(String name, String outerName,
			String innerName, int access) {
		cv.visitInnerClass(name, outerName, innerName, access);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		return cv.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public final void visitOuterClass(String owner, String name, String desc) {
		cv.visitOuterClass(owner, name, desc);
	}

	@Override
	public final void visitSource(String source, String debug) {
		cv.visitSource(source, debug);
	}

}