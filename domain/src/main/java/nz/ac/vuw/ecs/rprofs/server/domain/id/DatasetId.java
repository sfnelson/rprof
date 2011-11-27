package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/09/11
 */
public class DatasetId extends Id<DatasetId, Dataset> {

	public DatasetId() {
		super(0l);
	}

	public DatasetId(long id) {
		super(id);
	}

	public Class<Dataset> getTargetClass() {
		return Dataset.class;
	}

	public short getDatasetIndex() {
		return (short) getValue();
	}

	public void setDatasetIndex(short index) {
		// noop provided for GWT.
	}
}
