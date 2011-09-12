package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

public class InstanceId extends Id<InstanceId, Instance> {

	public static InstanceId create(Dataset dataset, long id) {
		if (id == 0) {
			return null;
		}
		return new InstanceId(dataset.getId().indexValue(), id);
	}

	private static final long mask = 0xFFFFFFFFFFFFl;

	public InstanceId() {
	}

	public InstanceId(long id) {
		super(id);
	}

	public InstanceId(short dataset, short thread, short index) {
		super((((long) dataset) << 48) | (((long) thread) << 32) | index);
	}

	public InstanceId(short dataset, long id) {
		super((((long) dataset) << 48) | (id & mask));
	}

	public Class<Instance> getTargetClass() {
		return Instance.class;
	}

	public short datasetValue() {
		return (short) ((longValue() >>> 48) & 0xFFFF);
	}

	public short threadValue() {
		return (short) (longValue() >>> 32);
	}

	public int indexValue() {
		return (int) longValue();
	}

	@Override
	public String toString() {
		return String.format("%d:%d.%d", datasetValue(), threadValue(), indexValue());
	}
}
