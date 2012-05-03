package nz.ac.vuw.ecs.rprofs.server.weaving;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzCreator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class ClassParser extends ClassVisitor {

	private final ClazzCreator<?> builder;

	public ClassParser(@NotNull ClazzCreator<?> builder) {
		super(Opcodes.ASM4);
		this.builder = builder;
	}

	public ClazzCreator<?> read(@NotNull byte[] classfile) {
		new org.objectweb.asm.ClassReader(classfile)
				.accept(this, org.objectweb.asm.ClassReader.SKIP_CODE);
		return builder;
	}

	@Override
	public void visit(int version, int access, @NotNull String name, @Nullable String signature,
					  @Nullable String superName, @Nullable String[] interfaces) {
		builder.setName(name);
		builder.setParentName(superName);
		builder.setAccess(access);
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
}
