package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;

import java.util.List;

public interface FieldService {

	Field getField(FieldId fieldId);

	List<? extends Field> findFields(ClazzId classId);

}
