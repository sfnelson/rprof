package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.server.data.util.Updater;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 3/05/12
 */
public class ReduceCache<I extends Id<I, T>, T extends DataObject<I, T>> extends Cache<I, T> {

	private final Updater<?, I, T> output;

	public ReduceCache(Updater<?, I, T> output) {
		this.output = output;
	}

	protected void store(I id, T value) {
		output.init(value).upsert(id);
	}
}
