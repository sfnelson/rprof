package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
abstract class MongoFieldBuilder extends MongoBuilder<MongoFieldBuilder, FieldId, Field>
		implements FieldCreator<MongoFieldBuilder>, FieldUpdater<MongoFieldBuilder>, FieldQuery<MongoFieldBuilder> {

	@VisibleForTesting
	MongoClassBuilder parent;

	@VisibleForTesting
	FieldId id;

	MongoFieldBuilder(MongoClassBuilder parent) {
		this.parent = parent;
	}

	/*
	 * We can't assign a field id until the owning class has an id, so delay the real store until the owning class has
	 * been stored. Return an placeholder id which will become valid when the class has been stored.
	 */
	@Override
	public FieldId store() {
		id = new FieldId(0l);
		parent.addField(this);
		return id; // will be set later.
	}

	/**
	 * Provided for {@link MongoClassBuilder} to store this field once it has obtained an id.
	 *
	 * @param owner	 the id that this field's owner class has been assigned
	 * @param ownerName the name of the owning class
	 * @return the updated field id
	 */
	FieldId store(ClazzId owner, String ownerName) {
		FieldId id = this.id;
		setOwner(owner);
		setOwnerName(ownerName);
		id.setValue(super.store().longValue());
		return id;
	}

	@Override
	protected void reset() {
		super.reset();
		id = null;
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
	public MongoFieldBuilder setOwner(ClazzId owner) {
		b.put("owner", owner.longValue());
		return this;
	}

	@Override
	public MongoFieldBuilder setOwnerName(String ownerName) {
		b.put("ownerName", ownerName);
		return this;
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
