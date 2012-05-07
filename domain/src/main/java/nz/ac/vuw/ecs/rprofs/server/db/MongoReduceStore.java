package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import nz.ac.vuw.ecs.rprofs.server.reports.Finisher;
import nz.ac.vuw.ecs.rprofs.server.reports.ReduceStore;
import nz.ac.vuw.ecs.rprofs.server.reports.Reducer;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 7/05/12
 */
class MongoReduceStore<I extends Id<I, T>, T extends DataObject<I, T>> extends Cache<I, T>
		implements ReduceStore<I, T> {
	private final Database database;
	private final DB db;
	private final DBCollection c;
	private final EntityBuilder<?, I, T> builder;
	private final Reducer<I, T> reducer;
	private final Finisher<I, T> finisher;
	private final String id;
	private final boolean lock;

	@SuppressWarnings("unchecked")
	public MongoReduceStore(Database database, DB db, DBCollection c, EntityBuilder<?, I, T> builder, Reducer<I, T> reducer, String id, boolean lock) {
		this.database = database;
		this.db = db;
		this.c = c;
		this.builder = builder;
		this.reducer = reducer;
		if (reducer instanceof Finisher) {
			this.finisher = (Finisher<I, T>) reducer;
		} else {
			this.finisher = null;
		}
		this.id = id;
		this.lock = lock;
	}

	@Override
	public void store(I id, T value) {
		if (containsKey(id)) {
			value = reducer.reduce(id, get(id), value);
		}
		put(id, value);
	}

	@Override
	public void flush(Map<I, T> toStore) {
		try {
			if (lock) database._lock(db, c, id);
			for (Map.Entry<I, T> e : toStore.entrySet()) {
				flush(e.getKey(), e.getValue());
			}
		} catch (Exception ex) {
			throw new RuntimeException("error flushing to " + c.getName(), ex);
		} finally {
			if (lock) database._unlock(db, c, id);
		}
	}

	private void flush(I id, T toStore) {
		T value = builder.init().find(id);
		boolean exists;
		if (value == null) {
			value = toStore;
			exists = false;
		} else {
			value = reducer.reduce(id, value, toStore);
			exists = true;
		}
		if (finisher != null) finisher.finish(id, value);
		builder.init(value);
		if (exists) builder.replace(id);
		else builder.store();
	}
}
