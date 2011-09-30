package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
public interface MapReduce<Input extends DataObject<?, Input>, OutId extends Id<OutId, Output>,
		Output extends DataObject<OutId, Output>> {

	interface Emitter<OutId extends Id<OutId, Output>, Output extends DataObject<OutId, Output>> {
		void emit(OutId id, Output value);
	}

	void map(Input input, Emitter<OutId, Output> emitter);

	Output reduce(Long id, List<Output> values);

}
