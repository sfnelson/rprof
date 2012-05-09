/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.domain.MethodUtils;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class GenericClassWeaver extends BasicClassWeaver {

	public GenericClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv, cr);
	}

	@Override
	public MethodVisitor visitMethod(int access, @NotNull String name, @NotNull String desc,
									 @Nullable String signature, @Nullable String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

		Method method = cr.getMethod(name, desc);

		if (method == null) {
			// we added it, so skip...
			return mv;
		}

		// check for: public static void main(String[])
		if (MethodUtils.isMain(method)) {
			mv = new MainMethodWeaver(cr, method, mv);
		}
		// check for <init>(..)
		else if (MethodUtils.isInit(method)) {
			mv = new InitMethodWeaver(cr, method, mv);
		}
		// check for: public boolean equals(Object)
		else if (MethodUtils.isEquals(method)) {
			mv = new EqualsMethodWeaver(cr, method, mv);
		}
		// check for: public int hashCode()
		else if (MethodUtils.isHashCode(method)) {
			mv = new HashCodeMethodWeaver(cr, method, mv);
		}
		// check for: public methods with arguments on a collection
		else if ((cr.getProperties() & Clazz.COLLECTION_MATCHED) != 0
				&& MethodUtils.isPublic(method) && MethodUtils.hasArgs(method)) {
			mv = new CollectionMethodWeaver(cr, method, mv);
		}
		return mv;
	}
}
