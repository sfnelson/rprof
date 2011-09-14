package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;

public class FieldId extends AttributeId<FieldId, Field> {

	public FieldId() {
	}

	public FieldId(short dataset, int type, short index) {
		super(dataset, type, index);
	}

	public FieldId(long id) {
		super(id);
	}

	public Class<Field> getTargetClass() {
		return Field.class;
	}

	public static FieldId create(Dataset ds, ClazzId type, short fnum) {
		return new FieldId(ds.getId().indexValue(), type.indexValue(), fnum);
	}
}
