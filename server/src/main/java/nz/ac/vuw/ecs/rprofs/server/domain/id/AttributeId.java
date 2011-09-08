package nz.ac.vuw.ecs.rprofs.server.domain.id;

import javax.persistence.MappedSuperclass;

import nz.ac.vuw.ecs.rprofs.server.model.Attribute;

@SuppressWarnings("serial")
@MappedSuperclass
public class AttributeId<T extends Attribute<T>> extends Id<T> {

	public AttributeId() {}

	public AttributeId(Long id) {
		super(id);
	}

	public AttributeId(short dataset, int type, short attribute) {
		super((((long) dataset) << 48) | (((long) type) << 16) | (attribute));
	}

	public short datasetValue() {
		return (short) ((longValue() >>> 48) & 0xFFFF);
	}
	public int typeValue() {
		return (int) ((longValue() >>> 16));
	}

	public short attributeValue() {
		return (short) (longValue() & 0xFFFF);
	}

	@Override
	public String toString() {
		return String.format("%d.%d.%d", datasetValue(), typeValue(), attributeValue());
	}
}
