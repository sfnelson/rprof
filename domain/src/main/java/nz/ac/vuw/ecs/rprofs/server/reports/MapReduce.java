package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
public interface MapReduce<Input extends DataObject<?, Input>, OutId extends Id<OutId, Output>,
		Output extends DataObject<OutId, Output>> extends Mapper<Input, OutId, Output>, Reducer<OutId, Output> {
}
