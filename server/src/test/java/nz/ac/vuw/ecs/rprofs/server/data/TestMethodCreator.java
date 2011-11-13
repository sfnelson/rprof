package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.data.util.MethodCreator;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class TestMethodCreator implements MethodCreator<TestMethodCreator> {
	TestClazzCreator clazzCreator;
	MethodId id;
	String name;
	String desc;
	int access;
	ClazzId owner;
	String ownerName;

	public TestMethodCreator(TestClazzCreator clazzCreator, MethodId id) {
		this.clazzCreator = clazzCreator;
		this.id = id;
	}

	@Override
	public TestMethodCreator init() {
		return this;
	}

	@Override
	public TestMethodCreator init(Method value) {
		return this;
	}

	@Override
	public TestMethodCreator setName(String name) {
		return this;
	}

	@Override
	public TestMethodCreator setDescription(String name) {
		return this;
	}

	@Override
	public TestMethodCreator setAccess(int access) {
		return this;
	}

	@Override
	public TestMethodCreator setOwner(ClazzId owner) {
		return this;
	}

	@Override
	public TestMethodCreator setOwnerName(String owner) {
		return this;
	}

	@Override
	public MethodId store() {
		clazzCreator.methods.add(new Method(id, 0, name, owner, ownerName, desc, access));
		return id;
	}

	@Override
	public Method get() {
		return new Method(id, 0, name, owner, ownerName, desc, access);
	}
}
