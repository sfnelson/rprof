package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.Creator;
import nz.ac.vuw.ecs.rprofs.server.data.Query;
import nz.ac.vuw.ecs.rprofs.server.data.Updater;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import org.bson.BSONObject;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public abstract class EntityBuilder<B extends EntityBuilder<B, I, T>, I extends Id<I, T>, T extends DataObject<I, T>>
		implements Creator<I, T>, Updater<I, T>, Query<I, T> {

	protected BasicDBObject b;

	@Override
	public I store() {
		I id = _createId();
		b.put("_id", id.longValue());
		_store(b);
		reset();
		return id;
	}

	@Override
	public long count() {
		long result = _count(b);
		reset();
		return result;
	}

	@Override
	public List<? extends T> find() {
		List<T> result = Lists.newArrayList();
		DBCursor c = _query(b);
		while (c.hasNext()) {
			init(c.next());
			result.add(get());
			reset();
		}
		c.close();
		return result;
	}

	@Override
	public void update(I toUpdate) {
		Long id = toUpdate.longValue();
		_update(new BasicDBObject("_id", id), b);
		reset();
	}

	protected void reset() {
		b.clear();
	}

	B init(@NotNull BSONObject init) {
		b.putAll(init);
		return (B) this;
	}

	abstract I _createId();

	abstract void _store(DBObject toStore);

	abstract void _update(DBObject ref, DBObject update);

	abstract DBCursor _query(DBObject ref);

	abstract long _count(DBObject ref);

	abstract T get();
}
