package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Locator;

import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.request.FieldService;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/10/11
 */
public class FieldManager extends Locator<Field, FieldId> implements FieldService {

	private final Database database;

	@Inject
	FieldManager(Database database) {
		this.database = database;
	}

	@Override
	public Field create(Class<? extends Field> aClass) {
		return new Field();
	}

	@Override
	public Field find(Class<? extends Field> aClass, FieldId fieldId) {
		return database.findEntity(fieldId);
	}

	@Override
	public Field getField(FieldId fieldId) {
		return database.findEntity(fieldId);
	}

	@Override
	public List<? extends Field> findFields(ClazzId classId) {
		return null;
	}

	@Override
	public Class<Field> getDomainType() {
		return Field.class;
	}

	@Override
	public FieldId getId(Field field) {
		return field.getId();
	}

	@Override
	public Class<FieldId> getIdType() {
		return FieldId.class;
	}

	@Override
	public Integer getVersion(Field field) {
		return field.getVersion();
	}
}
