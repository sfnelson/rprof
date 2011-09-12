package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import org.bson.BSONObject;

import javax.validation.constraints.NotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public abstract class MongoClassBuilder
		implements ClassManager.ClassBuilder, EntityBuilder<Clazz> {

	private BasicDBObject b;

	public MongoClassBuilder() {
		b = new BasicDBObject();
	}

	@Override
	public MongoClassBuilder init(@NotNull BSONObject init) {
		b.putAll(init);
		return this;
	}

	@Override
	public MongoClassBuilder setName(String name) {
		b.put("fqname", name);
		b.put("package", Clazz.getPackageName(name));
		b.put("short", Clazz.getSimpleName(name));
		return this;
	}

	@Override
	public MongoClassBuilder setParent(ClazzId parent) {
		b.put("parent", parent.longValue());
		return this;
	}

	@Override
	public MongoClassBuilder setProperties(int properties) {
		b.put("properties", properties);
		return this;
	}

	@Override
	public ClazzId store() {
		long id;
		if (!b.containsField("_id")) {
			id = _nextId();
			b.put("_id", id);
		} else {
			id = (Long) b.get("_id");
		}
		_store(b);
		b = new BasicDBObject();
		return new ClazzId(id);
	}

	@Override
	public Clazz get() {
		ClazzId id = new ClazzId((Long) b.get("_id"));
		int properties = (Integer) b.get("properties");
		Clazz clazz = new Clazz(id, null, null, null, 0);
		if (b.containsField("fqname")) {
			clazz.setName((String) b.get("fqname"));
		}
		if (b.containsField("parent")) {
			Long parentRaw = (Long) b.get("parent");
			if (parentRaw != null) {
				ClazzId parent = new ClazzId();
				clazz.setParent(parent);
			}
		}
		b = new BasicDBObject();
		return clazz;
	}

	abstract long _nextId();

	abstract void _store(DBObject data);
}
