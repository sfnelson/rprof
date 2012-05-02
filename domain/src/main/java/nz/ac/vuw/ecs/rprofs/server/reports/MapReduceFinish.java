package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.data.util.Updater;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 30/04/12
 */
public interface MapReduceFinish<I extends DataObject<?, I>, OID extends Id<OID, O>, O extends DataObject<OID, O>, U extends Updater<U, OID, O>>
		extends MapReduce<I, OID, O>, Finisher<OID, O, U> {
}
