package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.MethodCreator;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class TestClazzCreator implements ClazzCreator<TestClazzCreator> {
	Clazz c;
	public List<Method> methods;
	public List<Field> fields;

	public TestClazzCreator() {
	}

	public TestClazzCreator(ClazzId id) {
		start(id);
	}

	public TestClazzCreator start(ClazzId id) {
		c = new Clazz(id, 0, null, null, null, 0, 0, false);
		return this;
	}

	@Override
	public TestClazzCreator init() {
		return this;
	}

	@Override
	public TestClazzCreator init(Clazz value) {
		this.c = value;
		return this;
	}

	@Override
	public FieldCreator<?> addField() {
		return new TestFieldCreator(this,
				new FieldId(c.getId().getDatasetIndex(), c.getId().getClassIndex(),
						(short) (fields.size() + 1)));
	}

	@Override
	public MethodCreator<?> addMethod() {
		return new TestMethodCreator(this,
				new MethodId(c.getId().getDatasetIndex(), c.getId().getClassIndex(),
						(short) (methods.size() + 1)));
	}

	@Override
	public TestClazzCreator setName(String name) {
		c.setName(name);
		return this;
	}

	@Override
	public TestClazzCreator setSimpleName(String name) {
		return this;
	}

	@Override
	public TestClazzCreator setPackageName(String name) {
		return this;
	}

	@Override
	public TestClazzCreator setParent(ClazzId parent) {
		c.setParent(parent);
		return this;
	}

	@Override
	public TestClazzCreator setParentName(String name) {
		c.setParentName(name);
		return this;
	}

	@Override
	public TestClazzCreator setProperties(int properties) {
		c.setProperties(properties);
		return this;
	}

	@Override
	public TestClazzCreator setAccess(int access) {
		c.setAccess(access);
		return this;
	}

	@Override
	public TestClazzCreator setInitialized(boolean initialized) {
		c.setInitialized(initialized);
		return this;
	}

	@Override
	public ClazzId store() {
		return c.getId();
	}

	public Clazz get() {
		return c;
	}

	@Override
	public ClazzId storeIfNotInterface() {
		return store();
	}
}
