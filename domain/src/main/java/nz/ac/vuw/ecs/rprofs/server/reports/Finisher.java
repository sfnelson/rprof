package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 30/04/12
 */
public interface Finisher<OutId extends Id<OutId, Output>, Output extends DataObject<OutId, Output>> {
	void finish(OutId id, Output value);
}