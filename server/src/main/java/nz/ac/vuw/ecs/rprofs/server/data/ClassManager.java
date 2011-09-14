package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzCreator;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.request.ClazzService;
import nz.ac.vuw.ecs.rprofs.server.request.FieldService;
import nz.ac.vuw.ecs.rprofs.server.request.MethodService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ClassManager implements ClazzService, MethodService, FieldService {

	@VisibleForTesting
	@Autowired(required = true)
	Database database;

	public ClazzCreator createClazz() {
		return database.getClazzCreator();
	}

	@Override
	public Clazz getClazz(ClazzId id) {
		return database.findEntity(id);
	}

	@Override
	public Clazz getClazz(String name) {
		List<? extends Clazz> classes = database.getClazzQuery().setName(name).find();
		if (classes == null || classes.isEmpty()) return null;
		else return classes.get(0);
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
		return database.getClazzQuery().find();
	}

	public List<? extends Clazz> findClasses(String packageName) {
		return database.getClazzQuery().setPackageName(packageName).find();
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
		return database.getFieldQuery().setOwner(id).find();
	}

	@Override
	public List<? extends Method> findMethods(ClazzId id) {
		return database.getMethodQuery().setOwner(id).find();
	}

	public void setProperties(ClazzId clazzId, int properties) {
		database.getClazzUpdater().setProperties(properties).update(clazzId);
	}
}