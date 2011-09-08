package nz.ac.vuw.ecs.rprofs.server.domain.id;

import javax.persistence.Embeddable;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;

@SuppressWarnings("serial")
@Embeddable
public class FieldId extends AttributeId<Field> {

	public FieldId() {}

	public FieldId(short dataset, int type, short index) {
		super(dataset, type, index);
	}

	public FieldId(Long id) {
		super(id);
	}

	public static FieldId create(DataSet ds, Clazz type, short fnum) {
		return new FieldId(ds.getId().indexValue(), type.getId().indexValue(), fnum);
	}
}
