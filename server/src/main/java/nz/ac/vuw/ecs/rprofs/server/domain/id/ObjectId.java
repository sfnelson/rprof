package nz.ac.vuw.ecs.rprofs.server.domain.id;

import javax.persistence.Embeddable;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

@SuppressWarnings("serial")
@Embeddable
public class ObjectId extends Id<Instance> {

	public static ObjectId create(Dataset dataset, long id) {
		if (id == 0) {
			return null;
		}
		return new ObjectId(dataset.getId(), id);
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

	public short getDataset() {
		return (short) ((getId() >>> 48) & 0xFFFF);
	}

	public short getThread() {
		return (short) ((getId() >>> 32) & 0xFFFFFFFF);
	}

	public int getIndex() {
		return (int) (getId() & 0xFFFFFFFF);
	}

	@Override
	public String toString() {
		return String.format("%d:%d.%d", getDataset(), getThread(), getIndex());
	}
}
