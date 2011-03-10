package nz.ac.vuw.ecs.rprofs.server.domain.id;

import javax.persistence.Embeddable;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;

@SuppressWarnings("serial")
@Embeddable
public class FieldId extends AttributeId<Field> {

	public FieldId() {}

	public FieldId(short dataset, int type, short index) {
		super(dataset, type, index);
	}
}
