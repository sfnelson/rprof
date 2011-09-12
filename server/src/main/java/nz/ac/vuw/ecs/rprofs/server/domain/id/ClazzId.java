package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

public class ClazzId extends Id<ClazzId, Clazz> {

	public static ClazzId create(Dataset dataset, int cnum) {
		return new ClazzId(dataset.getId().indexValue(), cnum);
	}

	public static final Class<ClazzId> TYPE = ClazzId.class;

	private static final long mask = 0xFFFFFFFF;

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

	public short datasetValue() {
		return (short) ((longValue() >>> 48) & 0xFFFF);
	}

	public int indexValue() {
		return (int) longValue();
	}

	@Override
	public String toString() {
		return String.format("%d.%d", datasetValue(), indexValue());
	}
}
