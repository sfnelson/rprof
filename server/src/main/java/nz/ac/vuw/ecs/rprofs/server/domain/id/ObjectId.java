package nz.ac.vuw.ecs.rprofs.server.domain.id;

import javax.persistence.Embeddable;

import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

@SuppressWarnings("serial")
@Embeddable
public class ObjectId extends Id<Instance> {

	public static ObjectId create(DataSet dataSet, long id) {
		if (id == 0) {
			return null;
		}
		return new ObjectId(dataSet.getId().indexValue(), id);
	}

	private static final long mask = 0xFFFFFFFFFFFFl;

	public ObjectId() {}

	public ObjectId(long id) {
		super(id);
	}

	public ObjectId(short dataset, short thread, short index) {
		super((((long) dataset) << 48) | (((long) thread) << 32) | index);
	}

	public ObjectId(short dataset, long id) {
		super((((long) dataset) << 48) | (id & mask));
	}

	public short datasetValue() {
		return (short) ((longValue() >>> 48) & 0xFFFF);
	}

	public short threadValue() {
		return (short) (longValue() >>> 32);
	}

	public int indexValue() {
		return longValue().intValue();
	}

	@Override
	public String toString() {
		return String.format("%d:%d.%d", datasetValue(), threadValue(), indexValue());
	}
}
