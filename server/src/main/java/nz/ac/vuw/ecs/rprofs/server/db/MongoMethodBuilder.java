package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.server.data.util.MethodCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.MethodQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.MethodUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
abstract class MongoMethodBuilder extends MongoBuilder<MongoMethodBuilder, MethodId, Method>
		implements MethodCreator<MongoMethodBuilder>, MethodUpdater<MongoMethodBuilder>, MethodQuery<MongoMethodBuilder> {

	MongoClassBuilder parent;
	MethodId id;

	public MongoMethodBuilder(MongoClassBuilder parent) {
		this.parent = parent;
	}

	@Override
	public MongoMethodBuilder init(Method value) {
		b.append("_id", value.getId());
		if (value.getName() != null) setName(value.getName());
		if (value.getDescription() != null) setDescription(value.getDescription());
		if (value.getAccess() != 0) setAccess(value.getAccess());
		if (value.getOwner() != null) setOwner(value.getOwner());
		if (value.getOwnerName() != null) setOwnerName(value.getOwnerName());

		return this;
	}

	/*
		 * We can't assign a method id until the owning class has an id, so delay the real store until the owning class has
		 * been stored. Return an placeholder id which will become valid when the class has been stored.
		 */
	@Override
	public MethodId store() {
		id = new MethodId(0l);
		parent.addMethod(this);
		return id; // will be set later.
	}

	/**
	 * Provided for {@link MongoClassBuilder} to store this method once it has obtained an id.
	 *
	 * @param owner	 the id that this method's owner class has been assigned
	 * @param ownerName the name of the owning class
	 * @return the updated method id
	 */
	MethodId store(ClazzId owner, String ownerName) {
		MethodId id = this.id;
		setOwner(owner);
		setOwnerName(ownerName);
		id.setValue(super.store().getValue());
		return id;
	}

	@Override
	protected void reset() {
		super.reset();
		id = null;
	}

	@Override
	public MongoMethodBuilder setName(String name) {
		b.put("name", name);
		return this;
	}

	@Override
	public MongoMethodBuilder setDescription(String desc) {
		b.put("description", desc);
		return this;
	}

	@Override
	public MongoMethodBuilder setAccess(int access) {
		b.put("access", access);
		return this;
	}

	@Override
	public MongoMethodBuilder setOwner(ClazzId owner) {
		b.put("owner", owner.getValue());
		return this;
	}

	@Override
	public MongoMethodBuilder setOwnerName(String ownerName) {
		b.put("ownerName", ownerName);
		return this;
	}

	@Override
	public nz.ac.vuw.ecs.rprofs.server.domain.Method get() {
		MethodId id = new MethodId((Long) b.get("_id"));
		Integer version = (Integer) b.get("version");
		String name = (String) b.get("name");
		String desc = (String) b.get("description");
		int access = (Integer) b.get("access");
		ClazzId cid = new ClazzId((Long) b.get("owner"));
		String owner = (String) b.get("ownerName");
		reset();
		return new Method(id, version, name, cid, owner, desc, access);
	}
}
