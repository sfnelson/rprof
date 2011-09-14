package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;

import java.util.List;

public interface FieldService {

	List<? extends Field> findFields(ClazzId classId);

}
