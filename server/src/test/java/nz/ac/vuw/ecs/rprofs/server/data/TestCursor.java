package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.Iterator;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
class TestCursor<T extends DataObject<?, T>> implements Query.Cursor<T> {

	private List<T> input;
	private Iterator<T> iterator;

	public TestCursor(List<T> input) {
		this.input = input;
		this.iterator = input.iterator();
	}

	@Override
	public int count() {
		return input.size();
	}

	@Override
	public void close() {
		// do nothing.
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		return iterator.next();
	}

	@Override
	public void remove() {
		iterator.remove();
	}
}
