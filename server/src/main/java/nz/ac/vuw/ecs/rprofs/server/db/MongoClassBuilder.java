package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import nz.ac.vuw.ecs.rprofs.server.data.util.*;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;

import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public abstract class MongoClassBuilder extends MongoBuilder<MongoClassBuilder, ClazzId, Clazz>
		implements ClazzCreator<MongoClassBuilder>, ClazzUpdater<MongoClassBuilder>, ClazzQuery<MongoClassBuilder> {

	@VisibleForTesting
	List<MongoFieldBuilder> fields;

	@VisibleForTesting
	List<MongoMethodBuilder> methods;

	@Override
	public MongoClassBuilder init(Clazz value) {
		reset();
		b.put("_id", value.getId().getValue());
		if (value.getProperties() != 0) {
			setProperties(value.getProperties());
		}
		if (value.getName() != null) setName(value.getName());
		if (value.getParent() != null) setParent(value.getParent());
		if (value.getParentName() != null) setParentName(value.getParentName());

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
	public MongoClassBuilder setPackageName(String name) {
		b.put("package", name);
		return this;
	}

	@Override
	public MongoClassBuilder setSimpleName(String name) {
		b.put("short", name);
		return this;
	}

	@Override
	public MongoClassBuilder setParent(ClazzId parent) {
		b.put("parent", parent.getValue());
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
	public abstract FieldCreator<?> addField();

	/**
	 * Provided for {@link MongoFieldBuilder} to insert itself when store is called.
	 *
	 * @param field the field builder to hold until store
	 */
	void addField(MongoFieldBuilder field) {
		if (fields == null) {
			fields = Lists.newArrayList();
		}
		fields.add(field);
	}

	@Override
	public abstract MethodCreator<?> addMethod();

	/**
	 * Provided for {@link MongoMethodBuilder} to insert itself when store is called.
	 *
	 * @param method the method builder to hold until store
	 */
	void addMethod(MongoMethodBuilder method) {
		if (methods == null) {
			methods = Lists.newArrayList();
		}
		methods.add(method);
	}

	@Override
	public ClazzId store() {
		DBCursor c;

		// set parent using parent class retrieved from the database
		if (b.containsField("parentName")) {
			c = _query(new BasicDBObject("name", b.get("parentName")));
			if (c.hasNext()) {
				b.put("parent", c.next().get("_id"));
			}
			c.close();
		}

		// save some state, after calling super.store() all object state is lost.
		String name = (String) b.get("name");
		List<MongoMethodBuilder> methods = this.methods;
		List<MongoFieldBuilder> fields = this.fields;

		// store class into the database.
		ClazzId id = super.store();

		// find any classes which specify this class as the parent and update them
		c = _query(new BasicDBObject("parentName", name));
		while (c.hasNext()) {
			_update(c.next(), new BasicDBObject("parent", id.getValue()));
		}
		c.close();

		// store any methods associated with this class
		if (methods != null) {
			for (short i = 1; i <= methods.size(); i++) {
				methods.get(i - 1).store(id, name);
			}
		}

		// store any fields associated with this class
		if (fields != null) {
			for (short i = 1; i <= fields.size(); i++) {
				fields.get(i - 1).store(id, name);
			}
		}

		return id;
	}

	@Override
	public Clazz get() {
		ClazzId id = new ClazzId((Long) b.get("_id"));
		Integer version = (Integer) b.get("version");
		int properties = 0;
		if (b.containsField("properties")) {
			properties = (Integer) b.get("properties");
		}
		Clazz clazz = new Clazz(id, version, null, null, null, properties);
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

	@Override
	protected void reset() {
		super.reset();

		// don't clear, these lists are still needed.
		fields = null;
		methods = null;
	}
}
