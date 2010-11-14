/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.Context.ActiveContext;

import com.google.gwt.dev.asm.ClassVisitor;

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
