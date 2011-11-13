package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface Creator<B extends Creator<B, I, T>, I extends Id<I, T>, T extends DataObject<I, T>>
		extends Builder<B, I, T> {
	I store();

	T get();
}
