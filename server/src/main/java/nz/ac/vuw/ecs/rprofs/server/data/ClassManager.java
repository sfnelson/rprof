package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ClassManager implements ClazzService, MethodService, FieldService {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(ClassManager.class);

	public interface ClazzBuilder<C extends ClazzBuilder<C>> {
		C setName(String name);

		C setSimpleName(String name);

		C setPackageName(String name);

		C setParent(ClazzId parent);

		C setParentName(String name);

		C setProperties(int properties);
	}

	public interface ClazzCreator<C extends ClazzCreator<C>> extends ClazzBuilder<C>, Creator<ClazzId, Clazz> {
		FieldCreator<?> addField();

		MethodCreator<?> addMethod();
	}

	public interface ClazzUpdater<C extends ClazzUpdater<C>> extends ClazzBuilder<C>, Updater<ClazzId, Clazz> {
	}

	public interface ClazzQuery<C extends ClazzQuery<C>> extends ClazzBuilder<C>, Query<ClazzId, Clazz> {
	}

	public interface FieldBuilder<F extends FieldBuilder<F>> {
		F setName(String name);

		F setDescription(String name);

		F setAccess(int access);

		F setOwner(ClazzId owner);

		F setOwnerName(String owner);
	}

	public interface FieldCreator<F extends FieldCreator<F>> extends FieldBuilder<F>, Creator<FieldId, Field> {
	}

	public interface FieldUpdater<F extends FieldUpdater<F>> extends FieldBuilder<F>, Updater<FieldId, Field> {
	}

	public interface FieldQuery<F extends FieldQuery<F>> extends FieldBuilder<F>, Query<FieldId, Field> {
	}

	public interface MethodBuilder<M extends MethodBuilder<M>> {
		M setName(String name);

		M setDescription(String name);

		M setAccess(int access);

		M setOwner(ClazzId owner);

		M setOwnerName(String owner);
	}

	public interface MethodCreator<M extends MethodCreator<M>> extends MethodBuilder<M>, Creator<MethodId, Method> {
	}

	public interface MethodUpdater<M extends MethodUpdater<M>> extends MethodBuilder<M>, Updater<MethodId, Method> {
	}

	public interface MethodQuery<M extends MethodQuery<M>> extends MethodBuilder<M>, Query<MethodId, Method> {
	}

	@VisibleForTesting
	@Autowired(required = true)
	Context context;

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