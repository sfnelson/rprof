/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import org.objectweb.asm.ClassVisitor;

import java.util.Set;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class ThrowableClassWeaver extends GenericClassWeaver {

	public ThrowableClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv, cr);

		Set<Field> watches = cr.getWatches();
		for (Field f : watches) {
			if (f.getName().equals("stackTrace")
					&& f.getDescription().equals("[Ljava/lang/StackTraceElement;")) {
				watches.remove(f);
				break;
			}
		}
	}
}