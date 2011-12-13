package nz.ac.vuw.ecs.rprofs.server.data.util;

import java.util.Date;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface DatasetBuilder<D extends DatasetBuilder<D>> extends Builder<D, DatasetId, Dataset> {
	D setBenchmark(String benchmark);

	D setStarted(Date date);

	D setStopped(Date date);

	D setDatasetHandle(String handle);
}
