package nz.ac.vuw.ecs.rprofs.server.model;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public interface EventVisitor {
	void visitArrayAllocated(Event e);

	void visitClassInitialized(Event e);

	void visitClassWeave(Event e);

	void visitFieldRead(Event e);

	void visitFieldWrite(Event e);

	void visitObjectAllocated(Event e);

	void visitObjectFreed(Event e);

	void visitObjectTagged(Event e);

	void visitMethodEnter(Event e);

	void visitMethodException(Event e);

	void visitMethodReturn(Event e);
}