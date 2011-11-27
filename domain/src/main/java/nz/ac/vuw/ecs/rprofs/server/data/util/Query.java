package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

import javax.validation.constraints.NotNull;
import java.util.Iterator;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface Query<I extends Id<I, T>, T extends DataObject<I, T>> {

	interface Cursor<T extends DataObject<?, T>> extends Iterator<T> {
		int count();

		void close();
	}

	long count();

	@NotNull
	Cursor<? extends T> find();

	@NotNull
	Cursor<? extends T> find(long start, long limit);
}
