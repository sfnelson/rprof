package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.server.data.util.Creator;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 3/05/12
 */
public class MapCache<I extends Id<I, T>, T extends DataObject<I, T>> extends Cache<I, T> {

	private final Creator<?, I, T> output;

	public MapCache(Creator<?, I, T> output) {
		this.output = output;
	}

	protected void store(I id, T value) {
		output.init(value).store();
	}
}
