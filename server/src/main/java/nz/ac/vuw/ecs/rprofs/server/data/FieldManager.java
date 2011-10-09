package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.web.bindery.requestfactory.shared.Locator;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/10/11
 */
@Configurable
public class FieldManager extends Locator<Field, FieldId> {

	@Autowired
	Database database;

	@Override
	public Field create(Class<? extends Field> aClass) {
		return new Field();
	}

	@Override
	public Field find(Class<? extends Field> aClass, FieldId fieldId) {
		return database.findEntity(fieldId);
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
