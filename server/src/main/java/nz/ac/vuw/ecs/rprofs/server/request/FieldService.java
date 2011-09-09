package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;

import java.util.List;

public interface FieldService {

	List<? extends Field> findFields(Clazz cls);

}
