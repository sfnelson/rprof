package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;

import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class FieldId extends AttributeId<Field> {

	public FieldId() {
	}

	public FieldId(short dataset, int type, short index) {
		super(dataset, type, index);
	}

	public FieldId(Long id) {
		super(id);
	}

	public static FieldId create(Dataset ds, Clazz type, short fnum) {
		return new FieldId(ds.getId().indexValue(), type.getId().indexValue(), fnum);
	}

	public static FieldId create(Dataset ds, ClassId type, short fnum) {
		return new FieldId(ds.getId().indexValue(), type.indexValue(), fnum);
	}
}
