package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/12/11
 */
public interface Emitter<OutId extends Id<OutId, Output>, Output extends DataObject<OutId, Output>> {
	void emit(OutId id, Output value);
}