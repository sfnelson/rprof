package nz.ac.vuw.ecs.rprofs.server.data;

import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzUpdater;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 18/09/12
 */
public interface DataSource {
	<I extends Id<I, T>, T extends DataObject<I, T>> T findEntity(@NotNull I id);

	ClazzUpdater<?> getClazzUpdater();
}
