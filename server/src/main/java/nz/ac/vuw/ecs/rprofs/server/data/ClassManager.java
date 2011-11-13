package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Locator;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.request.ClazzService;
import nz.ac.vuw.ecs.rprofs.server.request.FieldService;
import nz.ac.vuw.ecs.rprofs.server.request.MethodService;

public class ClassManager extends Locator<Clazz, ClazzId>
		implements ClazzService, MethodService, FieldService {

	private final Database database;

	@Inject
	ClassManager(Database database) {
		this.database = database;
	}

	public ClazzCreator createClazz() {
		return database.getClazzCreator();
	}

	@Override
	public Clazz getClazz(ClazzId id) {
		return database.findEntity(id);
	}

	@Override
	public Clazz getClazz(String name) {
		Query.Cursor<? extends Clazz> classes = database.getClazzQuery().setName(name).find();
		Clazz result = null;
		if (classes.hasNext()) {
			result = classes.next();
		}
		classes.close();
		return result;
	}

	@Override
	public Method getMethod(MethodId methodId) {
		return database.findEntity(methodId);
	}

	@Override
	public Field getField(FieldId fieldId) {
		return database.findEntity(fieldId);
	}

	@Override
	public List<String> findPackages() {
		return database.findPackages();
	}

	@Override
	public long findNumPackages() {
		return database.countPackages();
	}

	@Override
	public List<? extends Clazz> findClasses() {
		List<Clazz> result = Lists.newArrayList();
		Query.Cursor<? extends Clazz> cursor = database.getClazzQuery().find();
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}
		cursor.close();
		return result;
	}

	public List<? extends Clazz> findClasses(String packageName) {
		List<Clazz> result = Lists.newArrayList();
		Query.Cursor<? extends Clazz> cursor = database.getClazzQuery().setPackageName(packageName).find();
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}
		cursor.close();
		return result;
	}

	@Override
	public long findNumClasses() {
		return database.getClazzQuery().count();
	}

	@Override
	public long findNumClasses(String packageName) {
		return database.getClazzQuery().setPackageName(packageName).count();
	}

	@Override
	public List<? extends Field> findFields(ClazzId id) {
		List<Field> result = Lists.newArrayList();
		Query.Cursor<? extends Field> cursor = database.getFieldQuery().setOwner(id).find();
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}
		cursor.close();
		return result;
	}

	@Override
	public List<? extends Method> findMethods(ClazzId id) {
		List<Method> result = Lists.newArrayList();
		Query.Cursor<? extends Method> cursor = database.getMethodQuery().setOwner(id).find();
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}
		cursor.close();
		return result;
	}

	public void setProperties(ClazzId clazzId, int properties) {
		database.getClazzUpdater().setProperties(properties).update(clazzId);
	}

	@Override
	public Clazz create(Class<? extends Clazz> aClass) {
		return new Clazz();
	}

	@Override
	public Clazz find(Class<? extends Clazz> aClass, ClazzId clazzId) {
		return database.findEntity(clazzId);
	}

	@Override
	public Class<Clazz> getDomainType() {
		return Clazz.class;
	}

	@Override
	public ClazzId getId(Clazz clazz) {
		return clazz.getId();
	}

	@Override
	public Class<ClazzId> getIdType() {
		return ClazzId.class;
	}

	@Override
	public Integer getVersion(Clazz clazz) {
		return clazz.getVersion();
	}
}