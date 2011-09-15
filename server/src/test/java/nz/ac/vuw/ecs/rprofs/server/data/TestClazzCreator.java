package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.MethodCreator;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;

import java.util.List;

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
		c = new Clazz(id, null, null, null, 0);
		return this;
	}

	@Override
	public FieldCreator<?> addField() {
		return new TestFieldCreator(this,
				new FieldId(c.getId().datasetValue(), c.getId().indexValue(),
						(short) (fields.size() + 1)));
	}

	@Override
	public MethodCreator<?> addMethod() {
		return new TestMethodCreator(this,
				new MethodId(c.getId().datasetValue(), c.getId().indexValue(),
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
	public ClazzId store() {
		return c.getId();
	}

	public Clazz get() {
		return c;
	}
}
