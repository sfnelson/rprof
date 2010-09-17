/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;

import com.google.gwt.dev.asm.ClassVisitor;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ThrowableClassWeaver extends GenericClassWeaver {

	public ThrowableClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv, cr);
		
		cr.removeWatch(cr.getField(cr.name, "stackTrace", "[Ljava/lang/StackTraceElement;"));
	}

}
