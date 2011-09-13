package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import org.objectweb.asm.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class ClassParser extends ClassAdapter {

	private final ClassManager.ClassBuilder builder;

	public ClassParser(@NotNull ClassManager.ClassBuilder builder) {
		super(null);
		this.builder = builder;
	}

	public ClassManager.ClassBuilder read(@NotNull byte[] classfile) {
		new org.objectweb.asm.ClassReader(classfile)
				.accept(this, org.objectweb.asm.ClassReader.SKIP_CODE);
		return builder;
	}

	@Override
	public void visit(int version, int access, @NotNull String name, @Nullable String signature,
					  @Nullable String superName, @Nullable String[] interfaces) {
		builder.setName(name);
		builder.setParentName(superName);
	}

	@Override
	public void visitSource(@Nullable String source, @Nullable String debug) {
		// nothing to do.
	}

	@Override
	public void visitOuterClass(@Nullable String owner, @Nullable String name, @Nullable String desc) {
		// nothing to do.
	}

	@Override
	public AnnotationVisitor visitAnnotation(@Nullable String desc, boolean visible) {
		return null; // nothing to do.
	}

	@Override
	public void visitAttribute(@Nullable Attribute attr) {
		// nothing to do.
	}

	@Override
	public void visitInnerClass(@Nullable String name, @Nullable String outerName,
								@Nullable String innerName, int access) {
		// nothing to do.
	}

	@Override
	public FieldVisitor visitField(int access, @NotNull String name, @NotNull String desc,
								   @Nullable String signature, @Nullable Object value) {
		builder.addField()
				.setName(name)
				.setDescription(desc)
				.setAccess(access)
				.store();
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, @NotNull String name, @NotNull String desc,
									 @Nullable String signature, @Nullable String[] exceptions) {
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
