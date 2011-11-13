package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.data.util.FieldCreator;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class TestFieldCreator implements FieldCreator<TestFieldCreator> {
	TestClazzCreator clazzCreator;
	FieldId id;
	String name;
	String desc;
	int access;
	ClazzId owner;
	String ownerName;

	public TestFieldCreator(TestClazzCreator clazzCreator, FieldId id) {
		this.clazzCreator = clazzCreator;
		this.id = id;
	}

	@Override
	public TestFieldCreator init() {
		return this;
	}

	@Override
	public TestFieldCreator init(Field value) {
		return this;
	}

	@Override
	public TestFieldCreator setName(String name) {
		return this;
	}

	@Override
	public TestFieldCreator setDescription(String name) {
		return this;
	}

	@Override
	public TestFieldCreator setAccess(int access) {
		return this;
	}

	@Override
	public TestFieldCreator setOwner(ClazzId owner) {
		return this;
	}

	@Override
	public TestFieldCreator setOwnerName(String owner) {
		return this;
	}

	@Override
	public FieldId store() {
		clazzCreator.fields.add(new Field(id, 0, name, owner, ownerName, desc, access));
		return id;
	}

	@Override
	public Field get() {
		return new Field(id, 0, name, owner, ownerName, desc, access);
	}
}
