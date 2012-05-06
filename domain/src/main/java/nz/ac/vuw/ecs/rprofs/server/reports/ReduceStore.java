package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 7/05/12
 */
public interface ReduceStore<I extends Id<I, T>, T extends DataObject<I, T>> extends Emitter<I, T> {

	void flush();

}
