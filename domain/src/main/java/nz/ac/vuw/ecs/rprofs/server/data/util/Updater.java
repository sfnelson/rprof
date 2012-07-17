package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface Updater<U extends Updater<U, I, T>, I extends Id<I, T>, T extends DataObject<I, T>> {
	void update(I toUpdate);

	void replace(I toUpdate);
}
