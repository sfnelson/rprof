package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.model.Attribute;

public abstract class AttributeId<I extends AttributeId<I, T>, T extends Attribute<I, T>>
		extends Id<I, T> {

	public AttributeId() {
	}

	public AttributeId(long id) {
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
