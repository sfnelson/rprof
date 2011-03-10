package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.request.FieldRequest;

public class FieldService extends AbstractService implements FieldRequest {

	@Override
	public List<? extends Field> findFields(Class cls) {
		return cls.getFields();
	}

}
