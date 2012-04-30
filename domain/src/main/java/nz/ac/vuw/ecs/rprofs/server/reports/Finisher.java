package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.data.util.Updater;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 30/04/12
 */
public interface Finisher<OutId extends Id<OutId, Output>, Output extends DataObject<OutId, Output>,
		U extends Updater<OutId, Output>> {

	interface FinisherTask {
		void finish();
	}

	void finish(OutId id, U updater);
}