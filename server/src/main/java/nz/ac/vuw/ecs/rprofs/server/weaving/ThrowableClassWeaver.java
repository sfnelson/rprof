/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import org.objectweb.asm.ClassVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ThrowableClassWeaver extends GenericClassWeaver {

	public ThrowableClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv, cr);

		for (FieldRecord fr: cr.watches) {
			if (fr.name.equals("stackTrace")
					&& fr.description.equals("[Ljava/lang/StackTraceElement;")) {
				cr.watches.remove(fr);
				break;
			}
		}
	}
}