package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.bson.BSONObject;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public abstract class MongoClassBuilder
		implements ClassManager.ClassBuilder, EntityBuilder<Clazz> {

	@VisibleForTesting
	BasicDBObject b;

	@VisibleForTesting
	List<BasicDBObject> fields;

	@VisibleForTesting
	List<BasicDBObject> methods;

	MongoClassBuilder() {
		b = new BasicDBObject();
	}

	@Override
	public MongoClassBuilder init(@NotNull BSONObject init) {
		b.putAll(init);
		return this;
	}

	@Override
	public MongoClassBuilder setName(String name) {
		b.put("name", name);
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
	public MongoClassBuilder setParentName(String parentName) {
		b.put("parentName", parentName);
		return this;
	}

	@Override
	public MongoClassBuilder setProperties(int properties) {
		b.put("properties", properties);
		return this;
	}

	@Override
	public MongoFieldBuilder addField() {
		return new MongoFieldBuilder(this);
	}

	void addField(BasicDBObject field) {
		if (fields == null) {
			fields = Lists.newArrayList();
		}
		fields.add(field);
	}

	@Override
	public MongoMethodBuilder addMethod() {
		return new MongoMethodBuilder(this);
	}

	void addMethod(BasicDBObject method) {
		if (methods == null) {
			methods = Lists.newArrayList();
		}
		methods.add(method);
	}

	@Override
	public ClazzId store() {
		ClazzId id;
		if (!b.containsField("_id")) {
			id = new ClazzId(_nextId());
			if (id.longValue() != 0) {
				b.put("_id", id.longValue());
			}
		} else {
			id = new ClazzId((Long) b.get("_id"));
		}
		_storeClass(b);

		if (methods != null) {
			for (short i = 1; i <= methods.size(); i++) {
				BasicDBObject m = methods.get(i - 1);
				MethodId mid = new MethodId(id.datasetValue(), id.indexValue(), i);
				m.put("_id", mid.longValue());
				m.put("owner", id.longValue());
				m.put("ownerName", b.get("name"));
				_storeMethod(m);
			}
		}

		if (fields != null) {
			for (short i = 1; i <= fields.size(); i++) {
				BasicDBObject f = fields.get(i - 1);
				FieldId fid = new FieldId(id.datasetValue(), id.indexValue(), i);
				f.put("_id", fid.longValue());
				f.put("owner", id.longValue());
				f.put("ownerName", b.get("name"));
				_storeField(f);
			}
		}

		b = new BasicDBObject();
		methods = null;
		fields = null;
		return id;
	}

	@Override
	public Clazz get() {
		ClazzId id = new ClazzId((Long) b.get("_id"));
		int properties = 0;
		if (b.containsField("properties")) {
			properties = (Integer) b.get("properties");
		}
		Clazz clazz = new Clazz(id, null, null, null, properties);
		if (b.containsField("name")) {
			clazz.setName((String) b.get("name"));
		}
		if (b.containsField("parent")) {
			Long parentRaw = (Long) b.get("parent");
			if (parentRaw != null) {
				ClazzId parent = new ClazzId(parentRaw);
				clazz.setParent(parent);
			}
		}
		if (b.containsField("parentName")) {
			clazz.setParentName((String) b.get("parentName"));
		}
		b = new BasicDBObject();
		return clazz;
	}

	abstract long _nextId();

	abstract void _storeClass(DBObject data);

	abstract void _storeField(DBObject data);

	abstract void _storeMethod(DBObject data);
}
