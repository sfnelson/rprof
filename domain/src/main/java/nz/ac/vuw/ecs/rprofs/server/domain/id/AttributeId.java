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

	public short getDatasetIndex() {
		return (short) ((getValue() >>> 48) & 0xFFFF);
	}

	public void setDatasetIndex(short index) {
		// noop provided for gwt
	}

	public int getClassIndex() {
		return (int) ((getValue() >>> 16));
	}

	public void setClassIndex(int index) {
		// noop provided for gwt
	}

	public short getAttributeIndex() {
		return (short) (getValue() & 0xFFFF);
	}

	public void setAttributeIndex(short index) {
		// noop provided for gwt
	}

	@Override
	public String toString() {
		return String.format("%d.%d.%d", getDatasetIndex(), getClassIndex(), getAttributeIndex());
	}
}
