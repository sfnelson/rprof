package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

public class ClazzId extends Id<ClazzId, Clazz> {

	public static ClazzId create(Dataset dataset, int cnum) {
		return new ClazzId(dataset.getId().getDatasetIndex(), cnum);
	}

	public ClazzId() {
	}

	public Class<Clazz> getTargetClass() {
		return Clazz.class;
	}

	public ClazzId(short dataset, int index) {
		super((((long) dataset) << 48) | index);
	}

	public ClazzId(long classId) {
		super(classId);
	}

	public short getDatasetIndex() {
		return (short) ((getValue() >>> 48) & 0xFFFF);
	}

	public void setDatasetIndex(short datasetIndex) {
		// noop provided for gwt
	}

	public int getClassIndex() {
		return (int) getValue();
	}

	public void setClassIndex(int classIndex) {
		// noop provided for gwt
	}

	@Override
	public String toString() {
		return String.format("%d.%d", getDatasetIndex(), getClassIndex());
	}
}
