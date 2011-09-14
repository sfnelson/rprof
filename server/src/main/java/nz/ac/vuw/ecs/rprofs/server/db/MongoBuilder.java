package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public abstract class MongoBuilder<B extends EntityBuilder<B, I, T>, I extends Id<I, T>, T extends DataObject<I, T>>
		extends EntityBuilder<B, I, T> {

	@Override
	void _store(DBObject toStore) {
		_getCollection().insert(toStore);
	}

	@Override
	void _update(DBObject ref, DBObject update) {
		_getCollection().update(ref, new BasicDBObject("$set", update));
	}

	@Override
	DBCursor _query(DBObject query) {
		return _getCollection().find(query);
	}

	@Override
	long _count(DBObject query) {
		return _getCollection().count(query);
	}

	abstract DBCollection _getCollection();
}
