package nz.ac.vuw.ecs.rprofs.server.data.util;

import java.util.Date;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface DatasetBuilder<D extends DatasetBuilder<D>> {
	D setHandle(String handle);

	D setStarted(Date date);

	D setStopped(Date date);

	D setProgram(String program);
}
