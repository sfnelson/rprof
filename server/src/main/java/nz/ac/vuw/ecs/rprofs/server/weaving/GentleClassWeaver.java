package nz.ac.vuw.ecs.rprofs.server.weaving;

import org.objectweb.asm.ClassVisitor;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/04/12
 */
public class GentleClassWeaver extends BasicClassWeaver {

	private boolean clinitCalled = false;

	GentleClassWeaver(ClassVisitor cv, ClassRecord cr) {
		super(cv, cr);

		cr.getWatches().clear();
	}
}
