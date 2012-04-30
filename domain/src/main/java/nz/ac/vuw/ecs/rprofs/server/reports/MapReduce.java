package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
public interface MapReduce<I extends DataObject<?, I>, OID extends Id<OID, O>, O extends DataObject<OID, O>>
		extends Mapper<I, OID, O>, Reducer<OID, O> {
}
