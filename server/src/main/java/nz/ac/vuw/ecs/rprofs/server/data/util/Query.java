package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface Query<I extends Id<I, T>, T extends DataObject<I, T>> {

	long count();

	@NotNull
	List<? extends T> find();

	@NotNull
	List<? extends T> find(int start, int limit);
}
