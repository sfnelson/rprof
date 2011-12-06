package nz.ac.vuw.ecs.rprofs.server.data.util;

import java.util.Iterator;

import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

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

	T find(I id);
}
