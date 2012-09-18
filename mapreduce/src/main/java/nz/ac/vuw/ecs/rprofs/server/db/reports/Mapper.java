package nz.ac.vuw.ecs.rprofs.server.db.reports;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/12/11
 */
public interface Mapper<Input extends DataObject<?, Input>, OutId extends Id<OutId, Output>,
		Output extends DataObject<OutId, Output>> {

	void map(Input input, Emitter<OutId, Output> emitter);
}