package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.BasicDBObject;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager.MethodBuilder;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.bson.BSONObject;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class MongoMethodBuilder implements MethodBuilder, EntityBuilder<Method> {

	@VisibleForTesting
	MongoClassBuilder parent;

	@VisibleForTesting
	BasicDBObject b;

	MongoMethodBuilder(MongoClassBuilder parent) {
		this.parent = parent;
		b = new BasicDBObject();
	}

	MongoMethodBuilder() {
		b = new BasicDBObject();
	}

	public MongoMethodBuilder init(BSONObject data) {
		b.putAll(data);
		return this;
	}

	@Override
	public MethodBuilder setName(String name) {
		b.put("name", name);
		return this;
	}

	@Override
	public MethodBuilder setDescription(String desc) {
		b.put("description", desc);
		return this;
	}

	@Override
	public MethodBuilder setAccess(int access) {
		b.put("access", access);
		return this;
	}

	@Override
	public void store() {
		parent.addMethod(b);
		b = new BasicDBObject();
	}

	@Override
	public nz.ac.vuw.ecs.rprofs.server.domain.Method get() {
		MethodId id = new MethodId((Long) b.get("_id"));
		String name = (String) b.get("name");
		String desc = (String) b.get("description");
		int access = (Integer) b.get("access");
		ClazzId cid = new ClazzId((Long) b.get("owner"));
		String owner = (String) b.get("ownerName");
		return new Method(id, name, cid, owner, desc, access);
	}
}
