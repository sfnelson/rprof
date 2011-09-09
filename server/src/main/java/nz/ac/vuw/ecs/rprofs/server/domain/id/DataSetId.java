package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/09/11
 */
public class DataSetId extends Id<Dataset> {

	public DataSetId(short id) {
		super(id);
	}

	public short indexValue() {
		return longValue().shortValue();
	}
}
