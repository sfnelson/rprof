/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.ActiveContext;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;

import org.objectweb.asm.ClassVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ThrowableClassWeaver extends GenericClassWeaver {

	public ThrowableClassWeaver(ActiveContext context, ClassVisitor cv, ClassRecord cr) {
		super(context, cv, cr);

		cr.removeWatch(cr.getField(cr.getName(), "stackTrace", "[Ljava/lang/StackTraceElement;"));
	}

}
