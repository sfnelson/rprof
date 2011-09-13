package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.BasicDBObject;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager.FieldBuilder;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import org.bson.BSONObject;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class MongoFieldBuilder implements FieldBuilder {

	@VisibleForTesting
	BasicDBObject b;

	@VisibleForTesting
	MongoClassBuilder parent;

	MongoFieldBuilder(MongoClassBuilder parent) {
		this.parent = parent;
		b = new BasicDBObject();
	}

	MongoFieldBuilder() {
		b = new BasicDBObject();
	}

	MongoFieldBuilder init(BSONObject data) {
		b.putAll(data);
		return this;
	}

	@Override
	public MongoFieldBuilder setName(String name) {
		b.put("name", name);
		return this;
	}

	@Override
	public MongoFieldBuilder setDescription(String desc) {
		b.put("description", desc);
		return this;
	}

	@Override
	public MongoFieldBuilder setAccess(int access) {
		b.put("access", access);
		return this;
	}

	@Override
	public void store() {
		parent.addField(b);
		b = new BasicDBObject();
	}

	@Override
	public Field get() {
		FieldId id = new FieldId((Long) b.get("_id"));
		String name = (String) b.get("name");
		String desc = (String) b.get("description");
		int access = (Integer) b.get("access");
		ClazzId cid = new ClazzId((Long) b.get("owner"));
		String owner = (String) b.get("ownerName");
		return new Field(id, name, cid, owner, desc, access);
	}
}
